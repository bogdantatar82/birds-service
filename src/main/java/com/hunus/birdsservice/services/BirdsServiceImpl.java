package com.hunus.birdsservice.services;

import static com.hunus.birdsservice.persistence.data.Birds.toBird;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hunus.birdsservice.dto.BirdDTO;
import com.hunus.birdsservice.dto.SightingDTO;
import com.hunus.birdsservice.exception.InternalException;
import com.hunus.birdsservice.persistence.BirdsDAO;
import com.hunus.birdsservice.persistence.data.Birds;
import com.hunus.birdsservice.persistence.data.Sightings;

@Service
public class BirdsServiceImpl implements BirdsService {
    private static final Logger logger = LoggerFactory.getLogger(BirdsServiceImpl.class);

    private final BirdsDAO birdsDAO;

    @Autowired
    public BirdsServiceImpl(BirdsDAO birdsDAO) {
        this.birdsDAO = birdsDAO;
    }

    @Override
    public UUID saveBird(BirdDTO bird) {
        if (isBirdInvalid(bird)) {
            String msg = String.format("Invalid bird: %s", bird);
            logger.warn(msg);
            throw new IllegalArgumentException(msg);
        }
        try {
            return birdsDAO.save(toBird(bird)).getId();
        } catch (Exception ex) {
            String msg = String.format("An error occurred when saving bird: %s", bird);
            logger.error(msg, ex);
            throw new InternalException(msg);
        }
    }

    @Override
    public Optional<BirdDTO> findBirdById(UUID birdId) {
        return birdsDAO.findById(birdId)
            .map(Birds::toBirdDTO);
    }

    @Override
    public Optional<BirdDTO> findBirdByNameAndColor(String name, String color) {
        return birdsDAO.findByNameAndColor(name, color)
            .map(Birds::toBirdDTO);
    }

    @Override
    public List<SightingDTO> getAllSightingsForBird(UUID birdId) {
        Birds bird = birdsDAO.findById(birdId)
            .orElseThrow(() -> new IllegalArgumentException("No bird found with id: " + birdId));
        return bird.getSightings().stream().map(Sightings::toSightingDTO).collect(Collectors.toList());
    }

    @Override
    public List<BirdDTO> getAllBirds() {
        return birdsDAO.findAll().stream().map(Birds::toBirdDTO).collect(Collectors.toList());
    }

    private static boolean isBirdInvalid(BirdDTO input) {
        return Optional.ofNullable(input)
            .filter(bird -> bird.getName() != null && !bird.getName().isBlank())
            .filter(bird -> bird.getHeight() > 0 && bird.getWeight() > 0)
            .isEmpty();
    }
}
