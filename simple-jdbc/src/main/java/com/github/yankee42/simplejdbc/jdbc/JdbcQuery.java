package com.github.yankee42.simplejdbc.jdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * A minimal utility that wraps the JDBC-API and makes working with is less painful.
 */
public class JdbcQuery {
    private final String sql;
    private final PreparedStatementSetter preparedStatementSetter;

    private JdbcQuery(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        this.sql = sql;
        this.preparedStatementSetter = preparedStatementSetter;
    }

    public static JdbcQuery forSimpleSql(final String sql) {
        return new JdbcQuery(sql, PreparedStatementSetter.noop());
    }

    public static JdbcQuery forPreparedSql(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        return new JdbcQuery(sql, preparedStatementSetter);
    }

    public void update(final DataSource dataSource) throws SQLException {
        new JdbcQueryExecutor(dataSource, preparedStatementSetter, sql).close();
    }

    public<T> Stream<T> select(final DataSource dataSource, final RowTransformer<T> rowTransformer) throws SQLException {
        final JdbcQueryExecutor jdbcQueryExecutor = new JdbcQueryExecutor(dataSource, preparedStatementSetter, sql);
        final ResultSetIterator<T> resultSetIterator = new ResultSetIterator<>(jdbcQueryExecutor.resultSet, rowTransformer);

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
            resultSetIterator, Spliterator.ORDERED | Spliterator.NONNULL
        ), false).onClose(jdbcQueryExecutor::close);
    }

    public<T> List<T> selectAsList(final DataSource dataSource, final RowTransformer<T> rowTransformer) throws SQLException {
        try(Stream<T> stream = select(dataSource, rowTransformer)) {
            return stream.collect(toList());
        }
    }

    public<T> T selectSingleRow(final DataSource dataSource, final RowTransformer<T> rowTransformer) throws SQLException {
        final List<T> list = selectAsList(dataSource, rowTransformer);
        if (list.size() != 1) {
            throw new RuntimeException("Expected 1 row, but found " + list.size() + " rows");
        }
        return list.get(0);
    }

    private static class ResultSetIterator<T> implements Iterator<T> {
        private static final Object SENTINAL = new Object();

        private final ResultSet resultSet;
        private final RowTransformer<T> rowTransformer;
        private T next;

        private ResultSetIterator(final ResultSet resultSet, final RowTransformer<T> rowTransformer) {
            this.resultSet = resultSet;
            this.rowTransformer = rowTransformer;
            advance();
        }

        @SuppressWarnings("unchecked")
        private void advance() {
            try {
                if (resultSet.next()) {
                    next = rowTransformer.transformRow(resultSet);
                } else {
                    next = (T) SENTINAL;
                }
            } catch (SQLException e) {
                throw new Error(e);
            }
        }

        @Override
        public boolean hasNext() {
            return next != SENTINAL;
        }

        @Override
        public T next() {
            final T result = next;
            advance();
            return result;
        }
    }

    private static class JdbcQueryExecutor {
        private Connection connection;
        private PreparedStatement preparedStatement;
        private ResultSet resultSet;
        private PreparedStatementSetter preparedStatementSetter;

        private JdbcQueryExecutor(final DataSource dataSource,
                                 final PreparedStatementSetter preparedStatementSetter,
                                 final String sql) throws SQLException {
            try {
                connection = dataSource.getConnection();
                this.preparedStatementSetter = preparedStatementSetter;
                preparedStatement = connection.prepareStatement(sql);
                this.preparedStatementSetter.setValues(preparedStatement);
                preparedStatement.execute();
                resultSet = preparedStatement.getResultSet();
            } catch (SQLException e) {
                close();
                throw e;
            }
        }

        private void close() {
            closeSilently(resultSet);
            closeSilently(preparedStatement);
            closeSilently(connection);
        }

        private void closeSilently(final AutoCloseable closable) {
            try {
                if (closable != null) {
                    closable.close();
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
