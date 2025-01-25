package com.hunus.birdsservice.services;

import static com.hunus.birdsservice.persistence.data.Sightings.toSighting;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.hunus.birdsservice.dto.SightingDTO;
import com.hunus.birdsservice.exception.InternalException;
import com.hunus.birdsservice.persistence.SightingsDAO;
import com.hunus.birdsservice.persistence.data.Sightings;
import com.hunus.birdsservice.utils.PageResult;

@Service
public class SightingsServiceImpl implements SightingsService {
    private static final Logger logger = LoggerFactory.getLogger(SightingsServiceImpl.class);

    private final SightingsDAO sightingsDAO;

    @Autowired
    public SightingsServiceImpl(SightingsDAO sightingsDAO) {
        this.sightingsDAO = sightingsDAO;
    }

    @Override
    public UUID saveSighting(UUID birdId, SightingDTO sighting) {
        if (birdId == null || isSightingInvalid(sighting)) {
            String msg = String.format("Invalid sighting: %s for bird with id: '%s'", sighting, birdId);
            logger.warn(msg);
            throw new IllegalArgumentException(msg);
        }
        try {
            return sightingsDAO.save(toSighting(birdId, sighting)).getId();
        } catch (Exception ex) {
            String msg = String.format("An error occurred when saving sighting: %s for bird with id: %s", sighting, birdId);
            logger.error(msg, ex);
            throw new InternalException(msg);
        }
    }

    @Override
    public Optional<SightingDTO> getSighting(UUID birdId, UUID sightingId) {
        Optional<Sightings> sighting = sightingsDAO.findById(sightingId);
        if (sighting.isEmpty()) {
            return Optional.empty();
        }
        return sighting.filter(s -> s.getBirdId().equals(birdId))
            .map(Sightings::toSightingDTO)
            .or(() -> {
                throw new IllegalArgumentException(String.format("Unable to get sighting with id: %s for bird with id: %s", sightingId, birdId));
            });
    }

    @Override
    public List<SightingDTO> getAllSightings() {
        return sightingsDAO.findAll().stream().map(Sightings::toSightingDTO).collect(Collectors.toList());
    }

    @Override
    public PageResult<SightingDTO> searchSightings(SearchParams searchParams) {
        validateSearchParams(searchParams);
        Specification<Sightings> specification = getSightingsSpecification(searchParams);
        Page<Sightings> searchResult = sightingsDAO.findAll(specification, searchParams.toPageRequest());
        List<SightingDTO> sightings = searchResult.map(Sightings::toSightingDTO)
            .stream()
            .collect(Collectors.toList());
        return new PageResult<>(sightings, searchResult.getPageable().getPageNumber(),
            searchResult.getPageable().getPageSize(), searchResult.getTotalElements());
    }

    private static void validateSearchParams(SearchParams params) {
        if (isDateTimeIntervalInvalid(params.getAfter(), params.getBefore())) {
            String msg = String.format("Invalid date-time interval: [%s - %s] when searching for sightings", params.getAfter(), params.getBefore());
            logger.warn(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    private static boolean isDateTimeIntervalInvalid(LocalDateTime afterDate, LocalDateTime beforeDate) {
        return Period.between(afterDate.toLocalDate(), beforeDate.toLocalDate()).isNegative();
    }

    private static Specification<Sightings> getSightingsSpecification(SearchParams params) {
        Specification<Sightings> specification = Specification.where(
            (root, query, builder) -> builder.equal(root.get("birdId"), params.getBirdId())
        );
        if (!isEmpty(params.getLocation())) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("location"), params.getLocation()));
        }
        return specification.and((root, query, builder) ->
            builder.and(
                builder.greaterThanOrEqualTo(root.get("dateTime"), params.getAfter()),
                builder.lessThanOrEqualTo(root.get("dateTime"), params.getBefore())
            )
        );
    }

    private static boolean isSightingInvalid(SightingDTO input) {
        return Optional.ofNullable(input)
            .filter(sighting -> sighting.getLocation() != null && !sighting.getLocation().isBlank())
            .filter(sighting -> sighting.getDateTime() != null)
            .isEmpty();
    }
}
