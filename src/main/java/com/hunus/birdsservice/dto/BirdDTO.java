package com.hunus.birdsservice.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BirdDTO {
    private UUID id;
    private String name;
    private String color;
    private int weight;
    private int height;
    private List<SightingDTO> sightings;
}