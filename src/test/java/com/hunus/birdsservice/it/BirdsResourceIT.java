package com.hunus.birdsservice.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.hunus.birdsservice.utils.BirdsServiceUtils.generateBird;
import static com.hunus.birdsservice.utils.BirdsServiceUtils.generateSighting;
import static com.hunus.birdsservice.utils.BirdsServiceUtils.jsonBody;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;

import com.hunus.birdsservice.dto.BirdDTO;
import com.hunus.birdsservice.dto.SightingDTO;
import org.junit.jupiter.api.Test;

public class BirdsResourceIT extends BaseIT {
    private static final String ENDPOINT = "/v0/birds";

    @BeforeEach
    void beforeEach() throws SQLException {
        clearDb();
    }

    @Test
    void searchSightings_should_returnPaginatedContentFromSecondPage() {
        // first save bird with sightings
        List<String> locations = List.of("Romania", "Bulgaria", "Greece", "Turkey");
        UUID birdId = saveBirdWithSightings("great bird", "red", locations);

        // save second bird
        given()
            .when()
                .contentType(JSON)
                .get(ENDPOINT + "/sightings/search?birdId=" + birdId + "&page=1&limit=2")
            .then()
                .statusCode(200)
                .body(hasJsonPath("$.sightings[0].birdId", equalTo(birdId.toString())))
                .body(hasJsonPath("$.sightings[0].location", equalTo(locations.get(2))))
                .body(hasJsonPath("$.sightings[1].birdId", equalTo(birdId.toString())))
                .body(hasJsonPath("$.sightings[1].location", equalTo(locations.get(3))));

    }

    private UUID saveBirdWithSightings(String birdName, String birdColor, List<String> locations) {
        // save bird
        BirdDTO bird = generateBird(birdName, birdColor);
        UUID birdId = given()
            .when()
                .contentType(JSON)
                .body(jsonBody(bird))
                .post(ENDPOINT)
            .then()
                .statusCode(201)
                .extract()
                .jsonPath().getUUID("birdId");

        // save sightings for bird
        for (String location : locations) {
            SightingDTO sighting = generateSighting(location);
            given()
                .when()
                    .contentType(JSON)
                    .body(jsonBody(sighting))
                    .post(ENDPOINT + "/" + birdId + "/sightings")
                .then()
                    .statusCode(201)
                    .extract()
                    .jsonPath().getUUID("birdId");
        }
        return birdId;
    }
}
