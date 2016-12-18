package com.github.yankee42.acmemanager;

import java.io.Reader;

public interface UserKeyRepository {
    Reader getUserKey();
    void saveUserKey(WriterWriter writerWriter);
}
