package com.hunus.birdsservice.persistence.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.hunus.birdsservice.dto.BirdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "birds")
@Getter
@Setter
@NoArgsConstructor
public class Birds extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "color", nullable = false)
    private String color;

    @Column(name = "height")
    private int height;

    @Column(name = "weight")
    private int weight;

    // the OWNED side in the bidirectional relationship
    @OneToMany(mappedBy = "birdId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Sightings> sightings = new ArrayList<>();

    public Birds(String name, String color, int height, int weight) {
        this.name = name;
        this.color = color;
        this.height = height;
        this.weight = weight;
    }

    public static BirdDTO toBirdDTO(Birds input) {
        return Optional.ofNullable(input)
            .map(bird -> new BirdDTO(
                input.id,
                input.name,
                input.color,
                input.weight,
                input.height,
                input.sightings.stream().map(Sightings::toSightingDTO).collect(Collectors.toList())
            )).orElse(null);
    }

    public static Birds toBird(BirdDTO input) {
        return new Birds(
            input.getName(),
            input.getColor(),
            input.getHeight(),
            input.getWeight()
        );
    }
}
