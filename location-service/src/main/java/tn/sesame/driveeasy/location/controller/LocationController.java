package tn.sesame.driveeasy.location.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.sesame.driveeasy.location.dto.*;
import tn.sesame.driveeasy.location.model.StatutLocation;
import tn.sesame.driveeasy.location.service.LocationService;

import java.time.LocalDate;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService service;

    public LocationController(LocationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageResponse<LocationDTO>> getLocations(
            @RequestParam(required = false) StatutLocation statut,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<LocationDTO> result = service.getLocations(statut, clientId, vehiculeId, dateDebut, dateFin, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationEnrichieDTO> getLocationById(@PathVariable Long id) {
        LocationEnrichieDTO dto = service.getEnrichedLocationById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<LocationDTO> createLocation(@Valid @RequestBody LocationCreateDTO dto) {
        LocationDTO created = service.createLocation(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/simulation")
    public ResponseEntity<LocationSimulationResponseDTO> simulatePrice(
            @Valid @RequestBody LocationSimulationRequestDTO req) {
        LocationSimulationResponseDTO sim = service.simulatePrice(req);
        return ResponseEntity.ok(sim);
    }

    @PatchMapping("/{id}/depart")
    public ResponseEntity<LocationDTO> demarrerLocation(@PathVariable Long id) {
        LocationDTO updated = service.demarrerLocation(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/retour")
    public ResponseEntity<LocationDTO> retournerVehicule(
            @PathVariable Long id,
            @Valid @RequestBody LocationRetourDTO dto) {
        LocationDTO updated = service.retournerVehicule(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/annulation")
    public ResponseEntity<LocationDTO> annulerLocation(@PathVariable Long id) {
        LocationDTO updated = service.annulerLocation(id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/statistiques")
    public ResponseEntity<LocationStatistiquesDTO> getStatistiques() {
        LocationStatistiquesDTO stats = service.getStatistiques();
        return ResponseEntity.ok(stats);
    }
}
