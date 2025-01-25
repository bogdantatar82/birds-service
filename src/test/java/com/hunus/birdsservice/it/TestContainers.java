package com.hunus.birdsservice.it;

import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy;

class TestContainers {
    private static final Logger log = LoggerFactory.getLogger(TestContainers.class);
    private static final String POSTGRES_IMAGE_NAME = "postgres:14.10";
    private static final String SERVICE_IMAGE_NAME = "hunus/birds-service:latest";

    static PostgreSQLContainer<?> postgresContainer;
    static GenericContainer<?> birdsServiceContainer;
    private static Network network;

    public static void start() {
        log.info("Starting integration test containers");
        createPostgresContainer();
        createBirdsServiceContainer();
        log.info("Integration test containers started");
    }

    static PostgreSQLContainer<?> createPostgresContainer() {
        if (postgresContainer != null) {
            return postgresContainer;
        }
        log.info("Starting Postgres container");
        postgresContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE_NAME)
            .withDatabaseName("birdsservicedb")
            .withUsername("birdsservice")
            .withPassword("birdsservice")
            .withExposedPorts(5432)
            .withEnv(getEnvironment())
            .withNetwork(getTestNetwork())
            .withNetworkAliases("birds-service-database");
        postgresContainer.start();
        return postgresContainer;
    }

    private static GenericContainer createBirdsServiceContainer() {
        if (birdsServiceContainer != null) {
            return birdsServiceContainer;
        }
        log.info("Starting BirdsService container");
        // If running in intellij and local changes, make sure to manually regenerate docker image!
        birdsServiceContainer = new GenericContainer<>(SERVICE_IMAGE_NAME)
            .withExposedPorts(8888, 8787)
            .withEnv(getEnvironment())
            .withNetwork(getTestNetwork())
            .withNetworkAliases("birds-service")
            .waitingFor(new DockerHealthcheckWaitStrategy().withStartupTimeout(Duration.ofSeconds(500)));
        birdsServiceContainer.start();
        return birdsServiceContainer;
    }

    private static Network getTestNetwork() {
        if (network == null) {
            network = Network.newNetwork();
        }
        return network;
    }

    private static Map<String, String> getEnvironment() {
        return Map.of("SPRING_PROFILES_ACTIVE", "it");
    }
}
