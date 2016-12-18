package com.github.yankee42.simplejdbc.jdbc;

import org.hsqldb.jdbc.JDBCDataSource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class JdbcQueryTest {

    private DataSource dataSource;

    @BeforeMethod
    public void setUp() throws Exception {
        JDBCDataSource jdbcDataSource = new JDBCDataSource();
        jdbcDataSource.setUrl("jdbc:hsqldb:mem:test");
        jdbcDataSource.setUser("sa");
        jdbcDataSource.setPassword("");
        jdbcDataSource.getConnection();

        try(Connection connection = jdbcDataSource.getConnection()) {
            try(Statement resetStatement = connection.createStatement()) {
                resetStatement.execute("drop schema public cascade");
            }

            try(Statement statement = connection.createStatement()) {
                statement.execute("create table test (str varchar(100))");
            }
        }

        this.dataSource = jdbcDataSource;
    }

    @Test
    public void insertUsingPreparedStatement_insertsData() throws Exception {
        // setup
        final String testString = "preparedStatement test";

        // execution
        JdbcQuery
            .forPreparedSql("insert into test values (?)", stmt -> stmt.setString(1, testString))
            .update(dataSource);

        // evaluation
        assertThat(readDbContent(), contains(testString));
    }

    @Test
    public void insertUsingSimpleStatement_insertsData() throws Exception {
        // setup
        final String testString = "simpleStatement test";

        // execution
        JdbcQuery
            .forSimpleSql("insert into test values ('" + testString + "')")
            .update(dataSource);

        // evaluation
        assertThat(readDbContent(), contains(testString));
    }

    @Test
    public void select_selectsValues() throws Exception {
        // setup
        final List<String> dbValues = Arrays.asList("a", "b");
        setDbContent(dbValues);

        // execution
        final List<String> actual;
        try(Stream<String> stream = JdbcQuery.forSimpleSql("select * from test").select(dataSource, rs -> rs.getString(1))) {
            actual = stream.collect(Collectors.toList());
        }

        // evaluation
        assertThat(actual, equalTo(dbValues));
    }

    private List<String> readDbContent() throws Exception {
        try(Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {
            statement.execute("select * from test");
            final List<String> result = new ArrayList<>();
            try(ResultSet resultSet = statement.getResultSet()) {
                while(resultSet.next()) {
                    result.add(resultSet.getString(1));
                }
                return result;
            }
        }
    }

    private void setDbContent(final Collection<String> values) throws Exception {
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("insert into test values (?)")) {
            for (final String value : values) {
                statement.setString(1, value);
                statement.execute();
            }
        }
    }
}
