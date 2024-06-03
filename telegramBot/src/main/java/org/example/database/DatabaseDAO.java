package org.example.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseDAO implements IDatabaseDAO {
    @Override
    public Connection getConnection() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", USER);
        properties.setProperty("password", PASSWORD);
        properties.setProperty("ssl", "true");
        properties.setProperty("sslcert", "/path/to/certificate.pem");
        return DriverManager.getConnection(URL, properties);
    }
}
