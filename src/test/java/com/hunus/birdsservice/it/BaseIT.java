package com.hunus.birdsservice.it;

import static com.hunus.birdsservice.it.TestContainers.birdsServiceContainer;
import static com.hunus.birdsservice.it.TestContainers.postgresContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public class BaseIT {
    private static Connection connection;

    @BeforeAll
    static void init() throws Exception {
        TestContainers.start();
        RestAssured.baseURI = getBaseURI();
        connection = DriverManager.getConnection(getJdbcUrl(), "birdsservice", "birdsservice");
    }

    protected void executeSql(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    protected void clearDb() throws SQLException {
        executeSql("truncate table birds, sightings;");
    }

    private static String getBaseURI() {
        return String.format("http://%s:%d/rest",
            birdsServiceContainer.getHost(),
            birdsServiceContainer.getMappedPort(8888)
        );
    }

    private static String getJdbcUrl() {
        return "jdbc:postgresql://localhost:" + postgresContainer.getFirstMappedPort() + "/birdsservicedb";
    }
}
