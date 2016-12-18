package com.github.yankee42.acmemanager;

import java.util.List;

public interface CertificateRepository {
    List<CertificateRecord> findCertificatesToRenew();
}
