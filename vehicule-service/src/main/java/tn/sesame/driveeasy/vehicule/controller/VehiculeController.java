package tn.sesame.driveeasy.vehicule.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.sesame.driveeasy.vehicule.dto.StatutUpdateDTO;
import tn.sesame.driveeasy.vehicule.dto.VehiculeDTO;
import tn.sesame.driveeasy.vehicule.model.Carburant;
import tn.sesame.driveeasy.vehicule.model.StatutVehicule;
import tn.sesame.driveeasy.vehicule.service.VehiculeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/vehicules")
public class VehiculeController {

    private final VehiculeService service;

    public VehiculeController(VehiculeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<VehiculeDTO>> getAllVehicules(
            @RequestParam(required = false) String marque,
            @RequestParam(required = false) Carburant carburant,
            @RequestParam(required = false) StatutVehicule statut,
            @RequestParam(required = false) BigDecimal prixMax) {
        List<VehiculeDTO> list = service.getAllVehicules(marque, carburant, statut, prixMax);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculeDTO> getVehiculeById(@PathVariable Long id) {
        VehiculeDTO dto = service.getVehiculeById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<VehiculeDTO> createVehicule(@Valid @RequestBody VehiculeDTO dto) {
        VehiculeDTO created = service.createVehicule(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculeDTO> updateVehicule(@PathVariable Long id, @Valid @RequestBody VehiculeDTO dto) {
        VehiculeDTO updated = service.updateVehicule(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicule(@PathVariable Long id) {
        service.deleteVehicule(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<VehiculeDTO> updateStatut(
            @PathVariable Long id,
            @Valid @RequestBody StatutUpdateDTO updateDTO) {
        VehiculeDTO updated = service.updateStatut(id, updateDTO.getStatut());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<VehiculeDTO>> getVehiculesDisponibles(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        List<VehiculeDTO> disponibles = service.getVehiculesDisponibles(dateDebut, dateFin);
        return ResponseEntity.ok(disponibles);
    }
}
