package com.github.yankee42.simplejdbc.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowTransformer<T> {
    T transformRow(ResultSet resultSet) throws SQLException;
}
