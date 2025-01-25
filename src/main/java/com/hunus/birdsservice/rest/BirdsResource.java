package com.hunus.birdsservice.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.hunus.birdsservice.dto.BirdDTO;
import com.hunus.birdsservice.dto.SightingDTO;
import com.hunus.birdsservice.services.BirdsService;
import com.hunus.birdsservice.services.SightingsService;
import com.hunus.birdsservice.utils.PageResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/v0/birds")
public class BirdsResource {
    private final BirdsService birdsService;
    private final SightingsService sightingsService;

    public BirdsResource(BirdsService birdsService, SightingsService sightingsService) {
        this.birdsService = birdsService;
        this.sightingsService = sightingsService;
    }

    @Operation(summary = "Add bird")
    @PostMapping(value = "", produces="application/json", consumes="application/json")
    @ApiResponse(responseCode = "201", description = "Bird saved successful")
    @ApiResponse(responseCode = "400", description = "Invalid client input")
    @ApiResponse(responseCode = "500", description = "Service throws server error")
    public ResponseEntity<SavedBirdResponse> addBird(
            @Parameter(description = "Sighting", required = true) @RequestBody BirdDTO bird) {
        UUID birdId = birdsService.saveBird(bird);
        SavedBirdResponse response = toSavedBirdResponse(birdId);
        return ResponseEntity.created(getBirdUri(birdId)).body(response);
    }

    @Operation(summary = "Get bird by id")
    @GetMapping(value = "/{birdId}", produces="application/json", consumes="application/json")
    @ApiResponse(responseCode = "200", description = "Bird returned successful")
    @ApiResponse(responseCode = "204", description = "No bird was found")
    public ResponseEntity<BirdDTO> getBird(
            @Parameter(description = "Bird id.", required = true) @PathVariable UUID birdId) {
        return birdsService.findBirdById(birdId)
            .map(bird -> ResponseEntity.ok().body(bird))
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "Get all birds")
    @GetMapping(value = "", produces="application/json", consumes="application/json")
    @ApiResponse(responseCode = "200", description = "All birds returned successful")
    public ResponseEntity<List<BirdDTO>> getAllBirds() {
        List<BirdDTO> allBirds = birdsService.getAllBirds();
        return ResponseEntity.ok().body(allBirds);
    }

    @Operation(summary = "Search bird by name and color")
    @GetMapping(value = "/search", produces="application/json", consumes="application/json")
    @ApiResponse(responseCode = "200", description = "Bird with name and color returned successful")
    @ApiResponse(responseCode = "204", description = "No bird was found")
    public ResponseEntity<BirdDTO> getBirdByNameAndColor(
            @Parameter(description = "Bird name", required = true) @RequestParam(value = "name") String birdName,
            @Parameter(description = "Bird color", required = true) @RequestParam(value = "color") String birdColor) {
        return birdsService.findBirdByNameAndColor(birdName, birdColor)
            .map(bird -> ResponseEntity.ok().body(bird))
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "Add sighting for a bird")
    @PostMapping(value = "/{birdId}/sightings", produces="application/json", consumes="application/json")
    @ApiResponse(responseCode = "201", description = "Sighting for bird saved successful")
    @ApiResponse(responseCode = "400", description = "Invalid client input")
    @ApiResponse(responseCode = "500", description = "Service throws server error")
    public ResponseEntity<SavedSightingResponse> addSightingForBird(
        @Parameter(description = "Bird id.", required = true) @PathVariable UUID birdId,
        @Parameter(description = "Sighting", required = true) @RequestBody SightingDTO sighting) {
        UUID sightingId = sightingsService.saveSighting(birdId, sighting);
        SavedSightingResponse response = toSavedSightingResponse(birdId, sightingId);
        return ResponseEntity.created(getSightingUri(birdId, sightingId)).body(response);
    }

    @Operation(summary = "Get sighting for a bird")
    @GetMapping(value = "/{birdId}/sightings/{sightingId}", produces="application/json", consumes="application/json")
    @ApiResponse(responseCode = "200", description = "Sighting for bird was found")
    @ApiResponse(responseCode = "204", description = "No sighting for bird was found")
    @ApiResponse(responseCode = "400", description = "Invalid client input")
    public ResponseEntity<SightingDTO> getSightingForBird(
            @Parameter(description = "Bird id.", required = true) @PathVariable UUID birdId,
            @Parameter(description = "Sighting id", required = true) @PathVariable UUID sightingId) {
        return sightingsService.getSighting(birdId, sightingId)
            .map(sighting -> ResponseEntity.ok().body(sighting))
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "Get all sightings for a bird")
    @GetMapping(value = "/{birdId}/sightings", produces="application/json", consumes="application/json")
    @ApiResponse(responseCode = "200", description = "Get all sightings for bird")
    @ApiResponse(responseCode = "400", description = "Invalid client input")
    public ResponseEntity<List<SightingDTO>> getAllSightingsForBird(
            @Parameter(description = "Bird id.", required = true) @PathVariable UUID birdId) {
        List<SightingDTO> sightings = birdsService.getAllSightingsForBird(birdId);
        return ResponseEntity.ok().body(sightings);
    }

    @Operation(summary = "Search sightings")
    @GetMapping(value = "/sightings/search", produces="application/json", consumes="application/json")
    @ApiResponse(responseCode = "200", description = "Get searched sightings for bird")
    @ApiResponse(responseCode = "400", description = "Invalid client input")
    public ResponseEntity<PageResult<SightingDTO>> searchSightings(
            @Parameter(description = "Bird id") @RequestParam(value = "birdId") UUID birdId,
            @Parameter(description = "Location") @RequestParam(value = "location", required = false) String location,
            @Parameter(description = "After") @RequestParam(value = "after", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
            @Parameter(description = "Before") @RequestParam(value = "before", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before,
            @Parameter(description = "Page") @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @Parameter(description = "Limit") @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit,
            @Parameter(description = "Ordering") @RequestParam(value = "ordering", required = false, defaultValue = "modified") String ordering) {
        LocalDateTime afterDate = Optional.ofNullable(after)
            .orElseGet(LocalDateTime::now);
        LocalDateTime beforeDate = Optional.ofNullable(before)
            .orElseGet(() -> afterDate.plusDays(3));
        SightingsService.SearchParams searchParams = SightingsService.SearchParams.builder()
            .birdId(birdId)
            .location(location)
            .after(afterDate)
            .before(beforeDate)
            .page(page)
            .limit(limit)
            .ordering(ordering)
            .build();
        PageResult<SightingDTO> sightings = sightingsService.searchSightings(searchParams);
        return ResponseEntity.ok().body(sightings);
    }

    @Operation(summary = "Get all sightings")
    @GetMapping(value = "/sightings/all", produces="application/json", consumes="application/json")
    @ApiResponse(responseCode = "200", description = "Get all sightings for all birds")
    public ResponseEntity<List<SightingDTO>> getAllSightings() {
        List<SightingDTO> allSightings = sightingsService.getAllSightings();
        return ResponseEntity.ok().body(allSightings);
    }

    private static URI getBirdUri(UUID birdId) {
        return UriComponentsBuilder
            .fromPath("birds/{birdId}")
            .buildAndExpand(birdId)
            .toUri();
    }

    private static URI getSightingUri(UUID birdId, UUID sightingId) {
        return UriComponentsBuilder
            .fromPath("birds/{birdId}/sightings/{sightingId}")
            .buildAndExpand(birdId, sightingId)
            .toUri();
    }

    private static SavedSightingResponse toSavedSightingResponse(UUID birdId, UUID sightingId) {
        return new SavedSightingResponse(birdId, sightingId).add(
            linkTo(
                methodOn(BirdsResource.class).getSightingForBird(birdId, sightingId)
            ).withSelfRel()
        );
    }

    private static SavedBirdResponse toSavedBirdResponse(UUID birdId) {
        return new SavedBirdResponse(birdId).add(
            linkTo(
                methodOn(BirdsResource.class).getBird(birdId)
            ).withSelfRel()
        );
    }
}
