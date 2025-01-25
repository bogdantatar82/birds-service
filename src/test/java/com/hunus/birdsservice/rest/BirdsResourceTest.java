package com.hunus.birdsservice.rest;

import static com.hunus.birdsservice.utils.BirdsServiceUtils.generateBird;
import static com.hunus.birdsservice.utils.BirdsServiceUtils.generateSighting;
import static com.hunus.birdsservice.utils.BirdsServiceUtils.getObjectMapper;
import static com.hunus.birdsservice.utils.BirdsServiceUtils.jsonBody;
import static com.hunus.birdsservice.utils.BirdsServiceUtils.readResponseAs;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hunus.birdsservice.dto.BirdDTO;
import com.hunus.birdsservice.dto.SightingDTO;
import com.hunus.birdsservice.exception.InternalException;
import com.hunus.birdsservice.exception.RestExceptionHandler;
import com.hunus.birdsservice.services.BirdsService;
import com.hunus.birdsservice.services.SightingsService;
import com.hunus.birdsservice.utils.PageResult;

@ExtendWith(MockitoExtension.class)
public class BirdsResourceTest {
    private static final String ENDPOINT = "/v0/birds";

    private MockMvc mockMvc;
    @Mock
    private BirdsService birdsService;
    @Mock
    private SightingsService sightingsService;
    @InjectMocks
    private BirdsResource resource;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(resource)
            .setControllerAdvice(new RestExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("Test successful on add bird")
    public void addBird_should_returnCreatedBirdIdWithSelfLink() throws Exception {
        // given
        UUID birdId = UUID.randomUUID();
        BirdDTO bird = generateBird();
        given(birdsService.saveBird(any(BirdDTO.class))).willReturn(birdId);

        // when
        MvcResult result = mockMvc.perform(
            post(ENDPOINT)
            .content(jsonBody(bird))
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated())
        .andReturn();

        // then
        SavedBirdResponse response = readResponseAs(result, SavedBirdResponse.class);
        assertEquals(birdId, response.getBirdId());
        assertTrue(response.getLink("self").isPresent());
        assertTrue(response.getLink("self").get().getHref().contains(ENDPOINT + "/" + birdId));
    }

    @ParameterizedTest
    @DisplayName("Test add bird when service throws client errors")
    @MethodSource("getClientExceptions")
    public void addBird_should_returnBadRequestStatus_when_serviceThrowsClientException(
            Class<? extends RuntimeException> exceptionClass) throws Exception {
        BirdDTO bird = generateBird();
        willThrow(exceptionClass).given(birdsService).saveBird(any(BirdDTO.class));

        mockMvc.perform(
            post(ENDPOINT)
            .content(jsonBody(bird))
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test add bird when service throws server error")
    public void addBird_should_returnInternalServerErrorStatus_when_serviceThrowsInternalException()
            throws Exception {
        BirdDTO bird = generateBird();
        willThrow(InternalException.class).given(birdsService).saveBird(any(BirdDTO.class));

        mockMvc.perform(
            post(ENDPOINT)
                .content(jsonBody(bird))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Test successful on get bird")
    public void getBird_should_returnBirdWithId() throws Exception {
        // given
        UUID birdId = UUID.randomUUID();
        BirdDTO bird = generateBird(birdId);
        given(birdsService.findBirdById(birdId)).willReturn(Optional.of(bird));

        // when
        MvcResult result = mockMvc.perform(
            get(ENDPOINT + "/" + birdId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn();

        // then
        BirdDTO response = readResponseAs(result, BirdDTO.class);
        assertEquals(birdId, response.getId());
        assertEquals(bird.getName(), response.getName());
        assertEquals(bird.getColor(), response.getColor());
        assertEquals(bird.getHeight(), response.getHeight());
        assertEquals(bird.getWeight(), response.getWeight());
    }

    @Test
    @DisplayName("Test no content on get bird")
    public void getBird_should_returnNoBird() throws Exception {
        // given
        UUID birdId = UUID.randomUUID();
        given(birdsService.findBirdById(birdId)).willReturn(Optional.empty());

        // when
        MvcResult result = mockMvc.perform(
            get(ENDPOINT + "/" + birdId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent())
        .andReturn();

        // then
        assertEquals("", result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Test successful on get all bird")
    public void getAllBirds_should_returnAllBirds() throws Exception {
        // given
        UUID birdId1 = UUID.randomUUID(), birdId2 = UUID.randomUUID();
        BirdDTO bird1 = generateBird(birdId1, "great bird", "red");
        BirdDTO bird2 = generateBird(birdId2, "awesome bird", "blue");
        given(birdsService.getAllBirds()).willReturn(List.of(bird1, bird2));

        // when
        MvcResult result = mockMvc.perform(
            get(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn();

        // then
        List<BirdDTO> response = getObjectMapper().readValue(
            result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertEquals(2, response.size());
        assertEquals(birdId1, response.get(0).getId());
        assertEquals(bird1.getName(), response.get(0).getName());
        assertEquals(bird1.getColor(), response.get(0).getColor());
        assertEquals(bird1.getHeight(), response.get(0).getHeight());
        assertEquals(bird1.getWeight(), response.get(0).getWeight());
        assertEquals(birdId2, response.get(1).getId());
        assertEquals(bird2.getName(), response.get(1).getName());
        assertEquals(bird2.getColor(), response.get(1).getColor());
        assertEquals(bird2.getHeight(), response.get(1).getHeight());
        assertEquals(bird2.getWeight(), response.get(1).getWeight());
    }

    @Test
    @DisplayName("Test successful on get bird by name and color")
    public void getBirdByNameAndColor_should_returnBirdWithNameAndColor() throws Exception {
        // given
        UUID birdId = UUID.randomUUID();
        String birdName = "great bird", birdColor = "red";
        BirdDTO bird = generateBird(birdId, birdName, birdColor);
        given(birdsService.findBirdByNameAndColor(birdName, birdColor)).willReturn(Optional.of(bird));

        // when
        MvcResult result = mockMvc.perform(
            get(ENDPOINT + "/search?name=" + birdName + "&color=" + birdColor)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn();

        // then
        BirdDTO response = readResponseAs(result, BirdDTO.class);
        assertEquals(birdId, response.getId());
        assertEquals(bird.getName(), response.getName());
        assertEquals(bird.getColor(), response.getColor());
        assertEquals(bird.getHeight(), response.getHeight());
        assertEquals(bird.getWeight(), response.getWeight());
    }

    @Test
    @DisplayName("Test no content on get bird by name and color")
    public void getBirdByNameAndColor_should_returnNoBirdWithNameAndColor() throws Exception {
        // given
        String birdName = "great bird", birdColor = "red";
        given(birdsService.findBirdByNameAndColor(birdName, birdColor)).willReturn(Optional.empty());

        // when
        MvcResult result = mockMvc.perform(
            get(ENDPOINT + "/search?name=" + birdName + "&color=" + birdColor)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent())
        .andReturn();

        // then
        assertEquals("", result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Test successful on add sighting for bird")
    public void addSightingForBird_should_returnCreatedSightingIdWithSelfLink() throws Exception {
        // given
        UUID birdId = UUID.randomUUID(), sightingId = UUID.randomUUID();
        SightingDTO sighting = generateSighting();
        given(sightingsService.saveSighting(eq(birdId), any(SightingDTO.class))).willReturn(sightingId);

        // when
        MvcResult result = mockMvc.perform(
            post(ENDPOINT + "/" + birdId + "/sightings")
                .content(jsonBody(sighting))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated())
        .andReturn();

        // then
        SavedSightingResponse response = readResponseAs(result, SavedSightingResponse.class);
        assertEquals(sightingId, response.getSightingId());
        assertEquals(birdId, response.getBirdId());
        assertTrue(response.getLink("self").isPresent());
        assertTrue(response.getLink("self").get().getHref().contains(ENDPOINT + "/" + birdId + "/sightings/" + sightingId));
    }

    @ParameterizedTest
    @DisplayName("Test add sighting for bird when service throws client errors")
    @MethodSource("getClientExceptions")
    public void addSightingForBird_should_returnBadRequestStatus_when_serviceThrowsClientException(
            Class<? extends RuntimeException> exceptionClass) throws Exception {
        UUID birdId = UUID.randomUUID();
        SightingDTO sighting = generateSighting();
        willThrow(exceptionClass).given(sightingsService).saveSighting(eq(birdId), any(SightingDTO.class));

        mockMvc.perform(
            post(ENDPOINT + "/" + birdId + "/sightings")
                .content(jsonBody(sighting))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test add sighting for bird when service throws server error")
    public void addSightingForBird_should_returnInternalServerErrorStatus_when_serviceThrowsInternalException()
            throws Exception {
        UUID birdId = UUID.randomUUID();
        SightingDTO sighting = generateSighting();
        willThrow(InternalException.class).given(sightingsService).saveSighting(eq(birdId), any(SightingDTO.class));

        mockMvc.perform(
            post(ENDPOINT + "/" + birdId + "/sightings")
                .content(jsonBody(sighting))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Test successful on get sighting for bird")
    public void getSightingForBird_should_returnBirdSightingWithId() throws Exception {
        // given
        UUID birdId = UUID.randomUUID(), sightingId = UUID.randomUUID();
        SightingDTO sighting = generateSighting(sightingId, birdId);
        given(sightingsService.getSighting(birdId, sightingId)).willReturn(Optional.of(sighting));

        // when
        MvcResult result = mockMvc.perform(
            get(ENDPOINT + "/" + birdId + "/sightings/" + sightingId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn();

        // then
        SightingDTO response = readResponseAs(result, SightingDTO.class);
        assertEquals(sightingId, response.getId());
        assertEquals(birdId, response.getBirdId());
        assertEquals(sighting.getLocation(), response.getLocation());
        assertEquals(sighting.getDateTime(), response.getDateTime());
    }

    @Test
    @DisplayName("Test no content on get sighting for bird")
    public void getSightingForBird_should_returnNoBirdSighting() throws Exception {
        // given
        UUID birdId = UUID.randomUUID(), sightingId = UUID.randomUUID();
        given(sightingsService.getSighting(birdId, sightingId)).willReturn(Optional.empty());

        // when
        MvcResult result = mockMvc.perform(
                get(ENDPOINT + "/" + birdId + "/sightings/" + sightingId)
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent())
        .andReturn();

        // then
        assertEquals("", result.getResponse().getContentAsString());
    }

    @ParameterizedTest
    @DisplayName("Test get sighting for bird when service throws client errors")
    @MethodSource("getClientExceptions")
    public void getSightingForBird_should_returnBadRequestStatus_when_serviceThrowsClientException(
            Class<? extends RuntimeException> exceptionClass) throws Exception {
        UUID birdId = UUID.randomUUID(), sightingId = UUID.randomUUID();
        willThrow(exceptionClass).given(sightingsService).getSighting(birdId, sightingId);

        mockMvc.perform(
            get(ENDPOINT + "/" + birdId + "/sightings/" + sightingId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test successful on get all sightings for bird")
    public void getAllSightingsForBird_should_returnAllBirdSightings() throws Exception {
        // given
        UUID birdId = UUID.randomUUID(), sightingId1 = UUID.randomUUID(), sightingId2 = UUID.randomUUID();
        SightingDTO sighting1 = generateSighting(sightingId1, birdId);
        SightingDTO sighting2 = generateSighting(sightingId2, birdId);
        given(birdsService.getAllSightingsForBird(birdId)).willReturn(List.of(sighting1, sighting2));

        // when
        MvcResult result = mockMvc.perform(
            get(ENDPOINT + "/" + birdId + "/sightings")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn();

        // then
        List<SightingDTO> response = getObjectMapper().readValue(
            result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertEquals(2, response.size());

        assertEquals(sightingId1, response.get(0).getId());
        assertEquals(birdId, response.get(0).getBirdId());
        assertEquals(sighting1.getLocation(), response.get(0).getLocation());
        assertEquals(sighting1.getDateTime(), response.get(0).getDateTime());

        assertEquals(sightingId2, response.get(1).getId());
        assertEquals(birdId, response.get(1).getBirdId());
        assertEquals(sighting2.getLocation(), response.get(1).getLocation());
        assertEquals(sighting2.getDateTime(), response.get(1).getDateTime());
    }

    @ParameterizedTest
    @DisplayName("Test get all sightings for bird when service throws client errors")
    @MethodSource("getClientExceptions")
    public void getAllSightingsForBird_should_returnBadRequestStatus_when_serviceThrowsClientException(
            Class<? extends RuntimeException> exceptionClass) throws Exception {
        UUID birdId = UUID.randomUUID();
        willThrow(exceptionClass).given(birdsService).getAllSightingsForBird(birdId);

        mockMvc.perform(
            get(ENDPOINT + "/" + birdId + "/sightings")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test search sightings for bird with search params returns paginated sightings result")
    public void searchSightings_should_returnPaginatedContent() throws Exception {
        // given
        UUID birdId = UUID.randomUUID(), sightingId1 = UUID.randomUUID(), sightingId2 = UUID.randomUUID();
        SightingDTO sighting1 = generateSighting(sightingId1, birdId);
        SightingDTO sighting2 = generateSighting(sightingId2, birdId);
        PageResult<SightingDTO> pageResult = new PageResult<>(List.of(sighting1, sighting2), 0, 2, 2L);
        given(sightingsService.searchSightings(any(SightingsService.SearchParams.class))).willReturn(pageResult);

        // when
        MvcResult result = mockMvc.perform(
            get(ENDPOINT + "/sightings/search?birdId=" + birdId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn();

        // then
        PageResult<SightingDTO> searchResult = getObjectMapper().readValue(
            result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertEquals(0, searchResult.getPage());
        assertEquals(2, searchResult.getSize());
        assertEquals(2, searchResult.getTotal());

        assertEquals(sightingId1, searchResult.getSightings().get(0).getId());
        assertEquals(birdId, searchResult.getSightings().get(0).getBirdId());
        assertEquals(sighting1.getLocation(), searchResult.getSightings().get(0).getLocation());
        assertEquals(sighting1.getDateTime(), searchResult.getSightings().get(0).getDateTime());

        assertEquals(sightingId2, searchResult.getSightings().get(1).getId());
        assertEquals(birdId, searchResult.getSightings().get(1).getBirdId());
        assertEquals(sighting2.getLocation(), searchResult.getSightings().get(1).getLocation());
        assertEquals(sighting2.getDateTime(), searchResult.getSightings().get(1).getDateTime());
    }

    @Test
    @DisplayName("Test search sightings for bird with no search params")
    public void searchSightings_should_callServiceWithDefaultParameters() throws Exception {
        UUID birdId = UUID.randomUUID();

        mockMvc.perform(
            get(ENDPOINT + "/sightings/search?birdId=" + birdId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn();

        ArgumentCaptor<SightingsService.SearchParams> paramsCaptor = ArgumentCaptor.forClass(SightingsService.SearchParams.class);
        verify(sightingsService).searchSightings(paramsCaptor.capture());
        SightingsService.SearchParams params = paramsCaptor.getValue();
        assertEquals(birdId, params.getBirdId());
        assertEquals(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), params.getAfter().truncatedTo(ChronoUnit.SECONDS));
        assertEquals(LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.SECONDS), params.getBefore().truncatedTo(ChronoUnit.SECONDS));
        assertNull(params.getLocation());
        assertEquals(0, params.getPage());
        assertEquals(10, params.getLimit());
        assertEquals("modified", params.getOrdering());
    }

    @Test
    @DisplayName("Test search sightings for bird with no results")
    public void searchSightings_should_returnEmptyPageResult_when_noResultsFound() throws Exception {
        UUID birdId = UUID.randomUUID();
        PageResult<SightingDTO> emptyPageResult = new PageResult<>(null, 0, 0, 0L);
        given(sightingsService.searchSightings(any(SightingsService.SearchParams.class))).willReturn(emptyPageResult);

        MvcResult result = mockMvc.perform(
            get(ENDPOINT + "/sightings/search?birdId=" + birdId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn();

        PageResult<SightingDTO> pageResult = getObjectMapper().readValue(
            result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertEquals(0, pageResult.getPage());
        assertEquals(0, pageResult.getSize());
        assertEquals(0, pageResult.getTotal());
        assertNull(pageResult.getSightings());
    }

    @Test
    @DisplayName("Test search sightings for bird when service throws client errors as missing required birdId param")
    public void searchSightings_should_returnBadRequestStatus_when_missingRequiredBirdIdParam() throws Exception {
        mockMvc.perform(
            get(ENDPOINT + "/sightings/search")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @DisplayName("Test search sightings for bird when service throws client errors")
    @MethodSource("getClientExceptions")
    public void searchSightings_should_returnBadRequestStatus_when_serviceThrowsClientException(
            Class<? extends RuntimeException> exceptionClass) throws Exception {
        UUID birdId = UUID.randomUUID();
        willThrow(exceptionClass).given(sightingsService).searchSightings(any());

        mockMvc.perform(
            get(ENDPOINT + "/sightings/search?birdId=" + birdId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    private static List<Class<? extends Exception>> getClientExceptions() {
        return List.of(IllegalArgumentException.class);
    }
}
