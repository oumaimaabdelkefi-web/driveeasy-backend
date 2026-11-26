package tn.sesame.driveeasy.client.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.sesame.driveeasy.client.dto.CategorieUpdateDTO;
import tn.sesame.driveeasy.client.dto.ClientDTO;
import tn.sesame.driveeasy.client.dto.EligibiliteDTO;
import tn.sesame.driveeasy.client.service.ClientService;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ClientDTO>> searchClients(@RequestParam(required = false) String query) {
        List<ClientDTO> clients = service.searchClients(query);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        ClientDTO client = service.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @PostMapping
    public ResponseEntity<ClientDTO> createClient(@Valid @RequestBody ClientDTO dto) {
        ClientDTO created = service.createClient(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> updateClient(@PathVariable Long id, @Valid @RequestBody ClientDTO dto) {
        ClientDTO updated = service.updateClient(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        service.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/eligibilite")
    public ResponseEntity<EligibiliteDTO> getEligibilite(@PathVariable Long id) {
        EligibiliteDTO eligibilite = service.checkEligibilite(id);
        return ResponseEntity.ok(eligibilite);
    }

    @PatchMapping("/{id}/categorie")
    public ResponseEntity<ClientDTO> updateCategorie(
            @PathVariable Long id,
            @Valid @RequestBody CategorieUpdateDTO updateDTO) {
        ClientDTO updated = service.updateCategorie(id, updateDTO.getCategorie());
        return ResponseEntity.ok(updated);
    }
}
