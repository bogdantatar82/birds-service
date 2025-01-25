package com.hunus.birdsservice.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.hunus.birdsservice.dto.BirdDTO;
import com.hunus.birdsservice.dto.SightingDTO;

public interface BirdsService {
    UUID saveBird(BirdDTO bird);

    Optional<BirdDTO> findBirdById(UUID birdId);

    Optional<BirdDTO> findBirdByNameAndColor(String birdName, String birdColor);

    List<BirdDTO> getAllBirds();

    List<SightingDTO> getAllSightingsForBird(UUID birdId);
}
