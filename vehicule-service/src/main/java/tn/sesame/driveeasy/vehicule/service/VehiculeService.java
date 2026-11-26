package tn.sesame.driveeasy.vehicule.service;

import org.springframework.stereotype.Service;
import tn.sesame.driveeasy.vehicule.dto.VehiculeDTO;
import tn.sesame.driveeasy.vehicule.exception.BusinessException;
import tn.sesame.driveeasy.vehicule.exception.DuplicateResourceException;
import tn.sesame.driveeasy.vehicule.exception.ResourceNotFoundException;
import tn.sesame.driveeasy.vehicule.model.Carburant;
import tn.sesame.driveeasy.vehicule.model.StatutVehicule;
import tn.sesame.driveeasy.vehicule.model.Vehicule;
import tn.sesame.driveeasy.vehicule.repository.VehiculeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehiculeService {

    private final VehiculeRepository repository;

    public VehiculeService(VehiculeRepository repository) {
        this.repository = repository;
    }

    public List<VehiculeDTO> getAllVehicules(String marque, Carburant carburant, StatutVehicule statut, BigDecimal prixMax) {
        return repository.findByFilters(marque, carburant, statut, prixMax).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public VehiculeDTO getVehiculeById(Long id) {
        Vehicule vehicule = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule introuvable avec l'identifiant : " + id));
        return toDTO(vehicule);
    }

    public VehiculeDTO createVehicule(VehiculeDTO dto) {
        validateBusinessRules(dto, null);

        if (repository.existsByImmatriculation(dto.getImmatriculation())) {
            throw new DuplicateResourceException("Un vehicule avec l'immatriculation " + dto.getImmatriculation() + " existe deja.");
        }

        Vehicule vehicule = toEntity(dto);
        if (vehicule.getStatut() == null) {
            vehicule.setStatut(StatutVehicule.DISPONIBLE);
        }
        Vehicule saved = repository.save(vehicule);
        return toDTO(saved);
    }

    public VehiculeDTO updateVehicule(Long id, VehiculeDTO dto) {
        Vehicule existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule introuvable avec l'identifiant : " + id));

        validateBusinessRules(dto, id);

        repository.findByImmatriculation(dto.getImmatriculation()).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new DuplicateResourceException("Un autre vehicule possede deja l'immatriculation " + dto.getImmatriculation());
            }
        });

        existing.setImmatriculation(dto.getImmatriculation());
        existing.setMarque(dto.getMarque());
        existing.setModele(dto.getModele());
        existing.setAnnee(dto.getAnnee());
        existing.setCouleur(dto.getCouleur());
        existing.setCarburant(dto.getCarburant());
        existing.setBoite(dto.getBoite());
        existing.setNbPlaces(dto.getNbPlaces());
        existing.setKilometrage(dto.getKilometrage());
        existing.setPrixJournalier(dto.getPrixJournalier());
        if (dto.getStatut() != null) {
            existing.setStatut(dto.getStatut());
        }
        existing.setImageUrl(dto.getImageUrl());

        return toDTO(repository.save(existing));
    }

    public void deleteVehicule(Long id) {
        Vehicule existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule introuvable avec l'identifiant : " + id));

        if (existing.getStatut() == StatutVehicule.LOUE) {
            throw new DuplicateResourceException("Suppression refusee : le vehicule " + existing.getImmatriculation() + " est actuellement loue.");
        }

        repository.deleteById(id);
    }

    public VehiculeDTO updateStatut(Long id, StatutVehicule nouveauStatut) {
        Vehicule existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule introuvable avec l'identifiant : " + id));
        existing.setStatut(nouveauStatut);
        return toDTO(repository.save(existing));
    }

    public List<VehiculeDTO> getVehiculesDisponibles(LocalDate dateDebut, LocalDate dateFin) {
        // Retourne les vehicules actuellement DISPONIBLE
        return repository.findAvailable().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void validateBusinessRules(VehiculeDTO dto, Long currentId) {
        int currentYear = Year.now().getValue();
        if (dto.getAnnee() != null && dto.getAnnee() > currentYear) {
            throw new BusinessException("L'annee du vehicule (" + dto.getAnnee() + ") ne peut pas exceder l'annee en cours (" + currentYear + ")");
        }
        if (dto.getPrixJournalier() != null && dto.getPrixJournalier().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Le prix journalier doit etre strictement positif.");
        }
    }

    public VehiculeDTO toDTO(Vehicule v) {
        if (v == null) return null;
        VehiculeDTO dto = new VehiculeDTO();
        dto.setId(v.getId());
        dto.setImmatriculation(v.getImmatriculation());
        dto.setMarque(v.getMarque());
        dto.setModele(v.getModele());
        dto.setAnnee(v.getAnnee());
        dto.setCouleur(v.getCouleur());
        dto.setCarburant(v.getCarburant());
        dto.setBoite(v.getBoite());
        dto.setNbPlaces(v.getNbPlaces());
        dto.setKilometrage(v.getKilometrage());
        dto.setPrixJournalier(v.getPrixJournalier());
        dto.setStatut(v.getStatut());
        dto.setImageUrl(v.getImageUrl());
        return dto;
    }

    public Vehicule toEntity(VehiculeDTO dto) {
        if (dto == null) return null;
        return new Vehicule(
                dto.getId(),
                dto.getImmatriculation(),
                dto.getMarque(),
                dto.getModele(),
                dto.getAnnee(),
                dto.getCouleur(),
                dto.getCarburant(),
                dto.getBoite(),
                dto.getNbPlaces(),
                dto.getKilometrage(),
                dto.getPrixJournalier(),
                dto.getStatut(),
                dto.getImageUrl()
        );
    }
}
