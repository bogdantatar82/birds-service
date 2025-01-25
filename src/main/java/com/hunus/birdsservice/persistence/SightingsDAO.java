package com.hunus.birdsservice.persistence;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hunus.birdsservice.persistence.data.Sightings;

public interface SightingsDAO extends JpaRepository<Sightings, UUID> {

//    Page<Sightings> findByBirdIdAndLocationAndDateTimeBetween(UUID birdId, String location,
//        LocalDateTime after, LocalDateTime before, Pageable pageable);

    Page<Sightings> findAll(Specification<Sightings> specification, Pageable pageable);
}
