package com.github.yankee42.acmeherokupsql;

import com.github.yankee42.acmemanager.CertificateRecord;
import com.github.yankee42.acmemanager.CertificateRenewer;
import com.github.yankee42.acmemanager.CertificateRepository;
import com.github.yankee42.acmemanager.RestChallengeFactory;
import com.github.yankee42.simplejdbc.jdbc.JdbcQuery;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.security.Security;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import static java.util.logging.LogManager.getLogManager;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        getLogManager().readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
        Security.addProvider(new BouncyCastleProvider());

        final Settings settings = PropertySettings.load(Main.class.getResourceAsStream("/settings.properties"));
        final HerokuDeploy herokuDeploy = new HerokuDeploy(
            settings.getHerokuToken(),
            new RegexDomainToAppName(settings.getDomainToAppNameRegex(), settings.getDomainToAppNameReplace())
        );
        final DataSource dataSource = createDataSource(settings);
        initDb(dataSource);

        final CertificateRepository certificateRepository = new JdbcCertificateRepository(dataSource);
        final List<CertificateRecord> recordsToRenew = certificateRepository.findCertificatesToRenew();

        new CertificateRenewer(
            settings.getAcmeEndpoint(),
            new JdbcUserKeyRepository(dataSource),
            (reg, agreement) -> {},
            new RestChallengeFactory(conn -> conn.setRequestProperty("X-PSK", settings.getTokenDeployPsk()))
        ).renewCertificates(recordsToRenew);

        for (final CertificateRecord certificateRecord : recordsToRenew) {
            log.info("Deploying certificate for {} to heroku", certificateRecord.getDomains());
            herokuDeploy.deploy(certificateRecord);
        }
        log.info("-------- DONE ----------");
    }

    private static DataSource createDataSource(final Settings settings) {
        final PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setUrl(settings.getDbUrl());
        dataSource.setUser(settings.getDbUser());
        dataSource.setPassword(settings.getDbPassword());
        return dataSource;
    }

    private static void initDb(final DataSource dataSource) throws SQLException {
        if (checkTablesMissing(dataSource)) {
            final InputStream sqlStream = Main.class.getResourceAsStream("/db.sql");
            if (sqlStream == null) {
                throw new Error("db.sql file is missing");
            }
            JdbcQuery.forSimpleSql(convertStreamToString(sqlStream)).update(dataSource);
        }
    }

    private static boolean checkTablesMissing(final DataSource dataSource) throws SQLException {
        return JdbcQuery
            .forSimpleSql("SELECT to_regclass('certificates')")
            .selectSingleRow(dataSource, rs -> rs.getObject(1)) == null;
    }

    private static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
