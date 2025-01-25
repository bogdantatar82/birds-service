package com.hunus.birdsservice.rest;

import java.util.UUID;

import org.springframework.hateoas.RepresentationModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class SavedSightingResponse extends RepresentationModel<SavedSightingResponse> {
    private UUID birdId;
    private UUID sightingId;
}
