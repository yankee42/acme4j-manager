package com.github.yankee42.acmeherokupsql;

import com.github.yankee42.acmemanager.WriterWriter;

import java.io.IOException;
import java.io.StringWriter;

public class WriterWriterToString {

    public static String writeToString(final WriterWriter writerWriter) {
        try {
            final StringWriter stringWriter = new StringWriter();
            writerWriter.write(stringWriter);
            return stringWriter.toString();
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
