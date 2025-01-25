package com.hunus.birdsservice.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hunus.birdsservice.dto.SightingDTO;
import com.hunus.birdsservice.exception.InternalException;
import com.hunus.birdsservice.persistence.SightingsDAO;
import com.hunus.birdsservice.persistence.data.Sightings;

@ExtendWith(MockitoExtension.class)
public class SightingsServiceImplTest {
    @Mock
    private SightingsDAO sightingsDAO;

    private SightingsService service;

    @BeforeEach
    public void setup() {
        service = new SightingsServiceImpl(sightingsDAO);
    }

    @Test
    void saveSighting_throwsIllegalArgumentExceptionWhenNullBirdIdInput() {
        try {
            service.saveSighting(null, new SightingDTO());
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            assertTrue(exception.getMessage().contains("Invalid sighting:"));
        }
    }

    @Test
    void saveSighting_throwsIllegalArgumentExceptionWhenNullSightingInput() {
        try {
            service.saveSighting(UUID.randomUUID(), null);
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            assertTrue(exception.getMessage().contains("Invalid sighting:"));
        }
    }

    @Test
    void saveSighting_throwsIllegalArgumentExceptionWhenSightingWithNullLocation() {
        try {
            service.saveSighting(UUID.randomUUID(), new SightingDTO());
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            assertTrue(exception.getMessage().contains("Invalid sighting:"));
        }
    }

    @Test
    void saveSighting_throwsIllegalArgumentExceptionWhenSightingWithBlankLocation() {
        try {
            service.saveSighting(UUID.randomUUID(), new SightingDTO(null, null, " ", LocalDateTime.now()));
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            assertTrue(exception.getMessage().contains("Invalid sighting:"));
        }
    }

    @Test
    void saveSighting_throwsIllegalArgumentExceptionWhenSightingWithNullDateTime() {
        try {
            service.saveSighting(UUID.randomUUID(), new SightingDTO(null, null, "Greece", null));
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            assertTrue(exception.getMessage().contains("Invalid sighting:"));
        }
    }

    @Test
    void saveSighting_returnsSavedSightingId() {
        // given
        UUID birdId = UUID.randomUUID(), sightingId = UUID.randomUUID();
        SightingDTO sightingDTO = new SightingDTO(null, null, "Greece", LocalDateTime.now());
        Sightings sighting = mock(Sightings.class);
        when(sighting.getId()).thenReturn(sightingId);
        when(sightingsDAO.save(any(Sightings.class))).thenReturn(sighting);

        // when
        UUID savedSightingId = service.saveSighting(birdId, sightingDTO);

        // then
        assertEquals(sightingId, savedSightingId);
    }

    @Test
    void saveSighting_throwsInternalExceptionWhenSavingFails() {
        // given
        UUID birdId = UUID.randomUUID();
        SightingDTO sightingDTO = new SightingDTO(null, null, "Greece", LocalDateTime.now());
        when(sightingsDAO.save(any(Sightings.class))).thenThrow(new RuntimeException());

        try {
            // when
            service.saveSighting(birdId, sightingDTO);
            Assertions.fail("Should have thrown InternalException");
        } catch (InternalException exception) {
            // then
            assertTrue(exception.getMessage().contains("An error occurred when saving sighting:"));
        }
    }

    @Test
    void getSighting_throwsInternalExceptionWhenNoSightingIsFound() {
        // given
        UUID birdId = UUID.randomUUID(), sightingId = UUID.randomUUID();
        when(sightingsDAO.findById(sightingId)).thenReturn(Optional.empty());

        // when
        Optional<SightingDTO> result = service.getSighting(birdId, sightingId);
        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void getSighting_throwsIllegalArgumentExceptionWhenNoSightingIsFound() {
        // given
        UUID birdId = UUID.randomUUID(), sightingId = UUID.randomUUID();
        when(sightingsDAO.findById(sightingId)).thenReturn(Optional.empty());

        // when
        Optional<SightingDTO> sighting = service.getSighting(birdId, sightingId);
        // then
        assertTrue(sighting.isEmpty());
    }

    @Test
    void getSighting_throwsIllegalArgumentExceptionWhenInvalidSightingIsFound() {
        // given
        UUID birdId = UUID.randomUUID(), anotherBirdId = UUID.randomUUID(), sightingId = UUID.randomUUID();
        Sightings sighting = mock(Sightings.class);
        when(sighting.getBirdId()).thenReturn(anotherBirdId);
        when(sightingsDAO.findById(sightingId)).thenReturn(Optional.of(sighting));

        try {
            // when
            service.getSighting(birdId, sightingId);
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            // then
            assertTrue(exception.getMessage().contains("Unable to get sighting with id:"));
        }
    }

    @Test
    void getSighting_returnsValidSighting() {
        // given
        UUID birdId = UUID.randomUUID(), sightingId = UUID.randomUUID();
        String location = "Greece";
        LocalDateTime dateTime = LocalDateTime.now();
        Sightings sighting = new Sightings(location, dateTime, birdId);
        when(sightingsDAO.findById(sightingId)).thenReturn(Optional.of(sighting));

        // when
        Optional<SightingDTO> returnedSighting = service.getSighting(birdId, sightingId);

        // then
        assertTrue(returnedSighting.isPresent());
        assertEquals(location, returnedSighting.get().getLocation());
        assertEquals(dateTime, returnedSighting.get().getDateTime());
        assertEquals(birdId, returnedSighting.get().getBirdId());
    }

    @Test
    void searchSightings_throwsIllegalArgumentExceptionWhenInvalidDateTimeInterval() {
        // given
        SightingsService.SearchParams searchParams = SightingsService.SearchParams.builder()
            .after(LocalDateTime.now())
            .before(LocalDateTime.now().minusDays(1))
            .build();

        try {
            // when
            service.searchSightings(searchParams);
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            // then
            assertTrue(exception.getMessage().contains("Invalid date-time interval:"));
        }
    }
}
