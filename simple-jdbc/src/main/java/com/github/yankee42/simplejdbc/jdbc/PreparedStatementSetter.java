package com.github.yankee42.simplejdbc.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementSetter {
    void setValues(PreparedStatement preparedStatement) throws SQLException;

    static PreparedStatementSetter noop() {
        return preparedStatement -> {};
    }
}
