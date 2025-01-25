package com.hunus.birdsservice.utils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.hunus.birdsservice.dto.BirdDTO;
import com.hunus.birdsservice.dto.SightingDTO;
import lombok.SneakyThrows;


public final class BirdsServiceUtils {

    private static final ObjectMapper objectMapper = getObjectMapper();

    private BirdsServiceUtils() {
    }

    public static BirdDTO generateBird() {
        return generateBird(null);
    }

    public static BirdDTO generateBird(UUID birdId) {
        return generateBird(birdId, "great bird", "red");
    }

    public static BirdDTO generateBird(String name, String color) {
        return generateBird(null, name, color);
    }

    public static BirdDTO generateBird(UUID birdId, String name, String color) {
        return new BirdDTO(birdId, name, color, 1, 1, List.of());
    }

    public static SightingDTO generateSighting() {
        return generateSighting(null, null);
    }

    public static SightingDTO generateSighting(String location) {
        return generateSighting(null, null, location);
    }

    public static SightingDTO generateSighting(UUID sightingId, UUID birdId) {
        return new SightingDTO(sightingId, birdId, "Greece", LocalDateTime.now());
    }

    public static SightingDTO generateSighting(UUID sightingId, UUID birdId, String location) {
        return new SightingDTO(sightingId, birdId, location, LocalDateTime.now());
    }

    public static <T> T readResponseAs(MvcResult result, Class<T> returnType) throws Exception {
        return objectMapper.readValue(
            result.getResponse().getContentAsString(), returnType);
    }

    @SneakyThrows
    public static String jsonBody(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
