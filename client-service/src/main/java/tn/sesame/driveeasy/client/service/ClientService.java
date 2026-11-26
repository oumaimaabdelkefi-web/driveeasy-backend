package tn.sesame.driveeasy.client.service;

import org.springframework.stereotype.Service;
import tn.sesame.driveeasy.client.dto.ClientDTO;
import tn.sesame.driveeasy.client.dto.EligibiliteDTO;
import tn.sesame.driveeasy.client.exception.DuplicateResourceException;
import tn.sesame.driveeasy.client.exception.ResourceNotFoundException;
import tn.sesame.driveeasy.client.model.CategorieClient;
import tn.sesame.driveeasy.client.model.Client;
import tn.sesame.driveeasy.client.repository.ClientRepository;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    public List<ClientDTO> searchClients(String query) {
        return repository.search(query).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ClientDTO getClientById(Long id) {
        Client client = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'identifiant : " + id));
        return toDTO(client);
    }

    public ClientDTO createClient(ClientDTO dto) {
        validateUniqueness(dto, null);

        Client client = toEntity(dto);
        if (client.getCategorieClient() == null) {
            client.setCategorieClient(CategorieClient.STANDARD);
        }
        Client saved = repository.save(client);
        return toDTO(saved);
    }

    public ClientDTO updateClient(Long id, ClientDTO dto) {
        Client existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'identifiant : " + id));

        validateUniqueness(dto, id);

        existing.setCin(dto.getCin());
        existing.setNom(dto.getNom());
        existing.setPrenom(dto.getPrenom());
        existing.setDateNaissance(dto.getDateNaissance());
        existing.setEmail(dto.getEmail());
        existing.setTelephone(dto.getTelephone());
        existing.setAdresse(dto.getAdresse());
        existing.setNumeroPermis(dto.getNumeroPermis());
        existing.setDateExpirationPermis(dto.getDateExpirationPermis());
        if (dto.getCategorieClient() != null) {
            existing.setCategorieClient(dto.getCategorieClient());
        }

        return toDTO(repository.save(existing));
    }

    public void deleteClient(Long id) {
        if (!repository.deleteById(id)) {
            throw new ResourceNotFoundException("Client introuvable avec l'identifiant : " + id);
        }
    }

    public EligibiliteDTO checkEligibilite(Long id) {
        Client client = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'identifiant : " + id));

        LocalDate today = LocalDate.now();
        int age = Period.between(client.getDateNaissance(), today).getYears();
        boolean ageOk = age >= 21;
        boolean permisOk = client.getDateExpirationPermis() != null && client.getDateExpirationPermis().isAfter(today);

        boolean eligible = ageOk && permisOk;
        StringBuilder motif = new StringBuilder();
        if (eligible) {
            motif.append("Client eligible pour la location de vehicules.");
        } else {
            if (!ageOk) {
                motif.append("Age insuffisant (").append(age).append(" ans, minimum 21 ans requis). ");
            }
            if (!permisOk) {
                motif.append("Permis de conduire expire depuis le ").append(client.getDateExpirationPermis()).append(". ");
            }
        }

        return new EligibiliteDTO(client.getId(), eligible, age, permisOk, motif.toString().trim());
    }

    public ClientDTO updateCategorie(Long id, CategorieClient nouvelleCategorie) {
        Client existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'identifiant : " + id));
        existing.setCategorieClient(nouvelleCategorie);
        return toDTO(repository.save(existing));
    }

    private void validateUniqueness(ClientDTO dto, Long currentId) {
        repository.findByCin(dto.getCin()).ifPresent(other -> {
            if (currentId == null || !other.getId().equals(currentId)) {
                throw new DuplicateResourceException("Un client possede deja le numero de CIN : " + dto.getCin());
            }
        });

        repository.findByEmail(dto.getEmail()).ifPresent(other -> {
            if (currentId == null || !other.getId().equals(currentId)) {
                throw new DuplicateResourceException("Un client possede deja l'adresse email : " + dto.getEmail());
            }
        });

        repository.findByNumeroPermis(dto.getNumeroPermis()).ifPresent(other -> {
            if (currentId == null || !other.getId().equals(currentId)) {
                throw new DuplicateResourceException("Un client possede deja le numero de permis : " + dto.getNumeroPermis());
            }
        });
    }

    public ClientDTO toDTO(Client c) {
        if (c == null) return null;
        ClientDTO dto = new ClientDTO();
        dto.setId(c.getId());
        dto.setCin(c.getCin());
        dto.setNom(c.getNom());
        dto.setPrenom(c.getPrenom());
        dto.setDateNaissance(c.getDateNaissance());
        dto.setEmail(c.getEmail());
        dto.setTelephone(c.getTelephone());
        dto.setAdresse(c.getAdresse());
        dto.setNumeroPermis(c.getNumeroPermis());
        dto.setDateExpirationPermis(c.getDateExpirationPermis());
        dto.setCategorieClient(c.getCategorieClient());
        dto.setDateInscription(c.getDateInscription());
        return dto;
    }

    public Client toEntity(ClientDTO dto) {
        if (dto == null) return null;
        return new Client(
                dto.getId(),
                dto.getCin(),
                dto.getNom(),
                dto.getPrenom(),
                dto.getDateNaissance(),
                dto.getEmail(),
                dto.getTelephone(),
                dto.getAdresse(),
                dto.getNumeroPermis(),
                dto.getDateExpirationPermis(),
                dto.getCategorieClient(),
                dto.getDateInscription()
        );
    }
}
