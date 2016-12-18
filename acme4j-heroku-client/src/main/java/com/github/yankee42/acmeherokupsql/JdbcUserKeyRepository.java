package com.github.yankee42.acmeherokupsql;

import com.github.yankee42.acmemanager.UserKeyRepository;
import com.github.yankee42.acmemanager.WriterWriter;
import com.github.yankee42.simplejdbc.jdbc.JdbcQuery;

import javax.sql.DataSource;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.List;

public class JdbcUserKeyRepository implements UserKeyRepository {
    private final DataSource dataSource;

    public JdbcUserKeyRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Reader getUserKey() {
        try {
            final List<String> keyPairs = JdbcQuery.forSimpleSql("select keyPair from UserAccounts").selectAsList(
                dataSource, rs -> rs.getString(1)
            );
            if (keyPairs.isEmpty()) {
                return null;
            }
            if (keyPairs.size() == 1) {
                return new StringReader(keyPairs.get(0));
            }
            throw new Error("Unexpected number of user account keypairs. Expected 0 or 1, but found " + keyPairs.size());
        } catch (SQLException e) {
            throw new Error(e);
        }
    }

    @Override
    public void saveUserKey(final WriterWriter writerWriter) {
        try {
            JdbcQuery
                .forPreparedSql(
                    "insert into UserAccounts (id,keyPair) values (1, ?) on conflict (id) do update set keyPair = EXCLUDED.keyPair",
                    stmt -> stmt.setString(1, WriterWriterToString.writeToString(writerWriter))
                ).update(dataSource);
        } catch (SQLException e) {
            throw new Error(e);
        }
    }
}
