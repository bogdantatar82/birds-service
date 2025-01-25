package com.hunus.birdsservice.persistence.data;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.hunus.birdsservice.dto.SightingDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sightings")
@Getter
@Setter
@NoArgsConstructor
public class Sightings extends BaseEntity {

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "datetime")
    private LocalDateTime dateTime;

    // the OWNER side in the bidirectional relationship
    @Column(name = "bird_id", nullable = false)
    private UUID birdId;

    public Sightings(String location, LocalDateTime dateTime, UUID birdId) {
        this.location = location;
        this.dateTime = dateTime;
        this.birdId = birdId;
    }

    public static SightingDTO toSightingDTO(Sightings input) {
        return Optional.ofNullable(input)
            .map(bird -> new SightingDTO(
                input.id,
                input.birdId,
                input.location,
                input.dateTime
            )).orElse(null);
    }

    public static Sightings toSighting(UUID birdId, SightingDTO input) {
        return new Sightings(
            input.getLocation(),
            input.getDateTime(),
            birdId
        );
    }
}
