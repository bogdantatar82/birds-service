package com.hunus.birdsservice.services;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hunus.birdsservice.dto.BirdDTO;
import com.hunus.birdsservice.dto.SightingDTO;
import com.hunus.birdsservice.exception.InternalException;
import com.hunus.birdsservice.persistence.BirdsDAO;
import com.hunus.birdsservice.persistence.data.Birds;
import com.hunus.birdsservice.persistence.data.Sightings;

@ExtendWith(MockitoExtension.class)
public class BirdsServiceImplTest {
    @Mock
    private BirdsDAO birdsDAO;

    private BirdsService service;

    @BeforeEach
    public void setup() {
        service = new BirdsServiceImpl(birdsDAO);
    }

    @Test
    void saveBird_throwsIllegalArgumentExceptionWhenNullInput() {
        try {
            service.saveBird(null);
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            assertTrue(exception.getMessage().contains("Invalid bird:"));
        }
    }

    @Test
    void saveBird_throwsIllegalArgumentExceptionWhenNullName() {
        BirdDTO bird = new BirdDTO();
        try {
            service.saveBird(bird);
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            assertTrue(exception.getMessage().contains("Invalid bird:"));
        }
    }

    @Test
    void saveBird_throwsIllegalArgumentExceptionWhenBlankName() {
        BirdDTO bird = new BirdDTO(null, "", "red", 1, 1, List.of());
        try {
            service.saveBird(bird);
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            assertTrue(exception.getMessage().contains("Invalid bird:"));
        }
    }

    @Test
    void saveBird_throwsIllegalArgumentExceptionWhenInvalidWeight() {
        BirdDTO bird = new BirdDTO(null, "", "red", -1, 1, List.of());
        try {
            service.saveBird(bird);
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            assertTrue(exception.getMessage().contains("Invalid bird:"));
        }
    }

    @Test
    void saveBird_throwsIllegalArgumentExceptionWhenInvalidHeight() {
        BirdDTO bird = new BirdDTO(null, "", "red", 1, -1, List.of());
        try {
            service.saveBird(bird);
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            assertTrue(exception.getMessage().contains("Invalid bird:"));
        }
    }

    @Test
    void saveBird_returnsSavedBirdId() {
        // given
        UUID birdId = UUID.randomUUID();
        BirdDTO birdDTO = new BirdDTO(null, "great bird", "red", 1, 1, List.of());
        Birds bird = mock(Birds.class);
        when(bird.getId()).thenReturn(birdId);
        when(birdsDAO.save(any(Birds.class))).thenReturn(bird);

        // when
        UUID savedBirdId = service.saveBird(birdDTO);

        // then
        assertEquals(birdId, savedBirdId);
    }

    @Test
    void saveBird_throwsInternalExceptionWhenSavingFails() {
        // given
        BirdDTO birdDTO = new BirdDTO(null, "great bird", "red", 1, 1, List.of());
        when(birdsDAO.save(any(Birds.class))).thenThrow(new RuntimeException());

        try {
            // when
            service.saveBird(birdDTO);
            Assertions.fail("Should have thrown InternalException");
        } catch (InternalException exception) {
            // then
            assertTrue(exception.getMessage().contains("An error occurred when saving bird:"));
        }
    }

    @Test
    void getAllSightingsForBird_throwsIllegalArgumentExceptionWhenNoBirdIsFound() {
        // given
        UUID birdId = UUID.randomUUID();
        when(birdsDAO.findById(birdId)).thenReturn(Optional.empty());

        try {
            // when
            service.getAllSightingsForBird(birdId);
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            // then
            assertTrue(exception.getMessage().contains("No bird found with id:"));
        }
    }

    @Test
    void getAllSightingsForBird_returnsAllBirdSightings() {
        // given
        UUID birdId = UUID.randomUUID();
        String location = "Greece";
        LocalDateTime dateTime = LocalDateTime.now();
        List<Sightings> sightings = List.of(new Sightings(location, dateTime, birdId));
        Birds bird = mock(Birds.class);
        when(bird.getSightings()).thenReturn(sightings);
        when(birdsDAO.findById(birdId)).thenReturn(Optional.of(bird));

        // when
        List<SightingDTO> sightingList = service.getAllSightingsForBird(birdId);

        // then
        assertFalse(sightingList.isEmpty());
        assertEquals(1, sightingList.size());
        assertEquals(location, sightingList.get(0).getLocation());
        assertEquals(dateTime, sightingList.get(0).getDateTime());
        assertEquals(birdId, sightingList.get(0).getBirdId());
    }
}
