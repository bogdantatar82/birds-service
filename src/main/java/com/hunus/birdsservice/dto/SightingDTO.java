package com.hunus.birdsservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SightingDTO {
    private UUID id;
    private UUID birdId;
    private String location;
    private LocalDateTime dateTime;
}
