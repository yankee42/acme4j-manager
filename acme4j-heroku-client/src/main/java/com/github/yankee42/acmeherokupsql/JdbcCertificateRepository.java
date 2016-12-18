package com.github.yankee42.acmeherokupsql;

import com.github.yankee42.acmemanager.CertificateRecord;
import com.github.yankee42.acmemanager.CertificateRepository;
import com.github.yankee42.simplejdbc.jdbc.JdbcQuery;
import com.github.yankee42.simplejdbc.jdbc.RowTransformer;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JdbcCertificateRepository implements CertificateRepository {
    private final DataSource dataSource;

    public JdbcCertificateRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<CertificateRecord> findCertificatesToRenew() {
        try {
            return JdbcQuery.forSimpleSql(
                "select id,domain,aliases,expires,keyPair,certificate,certificateChain from certificates where current_timestamp + interval '30 days' > expires"
            ).selectAsList(dataSource, new CertificateRowMapper(dataSource));
        } catch (SQLException e) {
            throw new Error(e);
        }
    }

    private static class CertificateRowMapper implements RowTransformer<CertificateRecord> {
        private final DataSource dataSource;

        private CertificateRowMapper(final DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public CertificateRecord transformRow(final ResultSet rs) throws SQLException {
            return new JdbcCertificateRecord(
                dataSource,
                rs.getInt("id"),
                rs.getString("domain"),
                parseAliases(rs.getString("aliases")),
                rs.getTimestamp("expires").toInstant(),
                rs.getString("keyPair"),
                rs.getString("certificate"),
                rs.getString("certificateChain")
            );
        }

        private ArrayList<String> parseAliases(final String aliases) throws SQLException {
            if (aliases.isEmpty()) {
                return new ArrayList<>();
            }
            return new ArrayList<>(Arrays.asList(aliases.split(",")));
        }
    }
}
