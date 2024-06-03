package org.example.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDatabaseDAO {
    final String URL = "jdbc:clickhouse://s39gbj1fw4.eu-central-1.aws.clickhouse.cloud:8443/default";
    final String USER = "default";
    final String PASSWORD = "ZD7oxk.q5q_Qd";

    public Connection getConnection() throws SQLException ;

}
