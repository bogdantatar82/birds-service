package com.hunus.birdsservice.services;

import static org.springframework.data.domain.Sort.Direction.ASC;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;

import com.hunus.birdsservice.dto.SightingDTO;
import com.hunus.birdsservice.utils.PageResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public interface SightingsService {

    UUID saveSighting(UUID birdId, SightingDTO sighting);

    Optional<SightingDTO> getSighting(UUID birdId, UUID sightingId);

    PageResult<SightingDTO> searchSightings(SearchParams searchParams);

    List<SightingDTO> getAllSightings();

    @Data
    @AllArgsConstructor
    @Builder
    final class SearchParams {
        private final UUID birdId;
        private final String location;
        private final LocalDateTime after;
        private final LocalDateTime before;
        private final Integer page;
        private final Integer limit;
        private final String ordering;

        public PageRequest toPageRequest() {
            return PageRequest.of(page, limit, ASC, ordering);
        }
    }
}
