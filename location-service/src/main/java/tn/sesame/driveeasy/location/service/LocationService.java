package tn.sesame.driveeasy.location.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.sesame.driveeasy.location.client.ClientRemoteDTO;
import tn.sesame.driveeasy.location.client.ClientServiceClient;
import tn.sesame.driveeasy.location.client.EligibiliteRemoteDTO;
import tn.sesame.driveeasy.location.client.VehiculeRemoteDTO;
import tn.sesame.driveeasy.location.client.VehiculeServiceClient;
import tn.sesame.driveeasy.location.dto.*;
import tn.sesame.driveeasy.location.exception.BusinessException;
import tn.sesame.driveeasy.location.exception.ResourceNotFoundException;
import tn.sesame.driveeasy.location.model.Location;
import tn.sesame.driveeasy.location.model.StatutLocation;
import tn.sesame.driveeasy.location.repository.LocationRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository repository;
    private final ClientServiceClient clientServiceClient;
    private final VehiculeServiceClient vehiculeServiceClient;

    public LocationService(LocationRepository repository,
                           ClientServiceClient clientServiceClient,
                           VehiculeServiceClient vehiculeServiceClient) {
        this.repository = repository;
        this.clientServiceClient = clientServiceClient;
        this.vehiculeServiceClient = vehiculeServiceClient;
    }

    public PageResponse<LocationDTO> getLocations(StatutLocation statut, Long clientId, Long vehiculeId,
                                                  LocalDate dateDebut, LocalDate dateFin, int page, int size) {
        List<Location> all = repository.findByFilters(statut, clientId, vehiculeId, dateDebut, dateFin);
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 10 : size;

        int fromIndex = safePage * safeSize;
        if (fromIndex >= all.size()) {
            return new PageResponse<>(Collections.emptyList(), safePage, safeSize, all.size());
        }
        int toIndex = Math.min(fromIndex + safeSize, all.size());
        List<LocationDTO> pageContent = all.subList(fromIndex, toIndex).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(pageContent, safePage, safeSize, all.size());
    }

    public LocationEnrichieDTO getEnrichedLocationById(Long id) {
        Location location = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location introuvable avec l'identifiant : " + id));

        LocationEnrichieDTO enriched = new LocationEnrichieDTO();
        mapCommonFields(location, enriched);

        try {
            ClientRemoteDTO client = clientServiceClient.getClientById(location.getClientId());
            enriched.setClient(client);
        } catch (Exception e) {
            log.warn("Impossible de recuperer les details du client {} : {}", location.getClientId(), e.getMessage());
        }

        try {
            VehiculeRemoteDTO vehicule = vehiculeServiceClient.getVehiculeById(location.getVehiculeId());
            enriched.setVehicule(vehicule);
        } catch (Exception e) {
            log.warn("Impossible de recuperer les details du vehicule {} : {}", location.getVehiculeId(), e.getMessage());
        }

        return enriched;
    }

    public LocationDTO createLocation(LocationCreateDTO dto) {
        if (dto.getDateFinPrevue().isBefore(dto.getDateDebut())) {
            throw new BusinessException("La date de fin prevue doit etre posterieure ou egale a la date de debut.");
        }

        // 1. Verification eligibilite client (inter-services)
        EligibiliteRemoteDTO eligibilite;
        try {
            eligibilite = clientServiceClient.getEligibilite(dto.getClientId());
        } catch (Exception e) {
            throw new BusinessException("Impossible de verifier l'eligibilite du client (service indisponible ou client inexistant).");
        }

        if (!eligibilite.isEligible()) {
            throw new BusinessException("Client non eligible : " + eligibilite.getMotif());
        }

        ClientRemoteDTO client = clientServiceClient.getClientById(dto.getClientId());

        // 2. Verification disponibilite vehicule (inter-services)
        VehiculeRemoteDTO vehicule;
        try {
            vehicule = vehiculeServiceClient.getVehiculeById(dto.getVehiculeId());
        } catch (Exception e) {
            throw new BusinessException("Vehicule introuvable avec l'identifiant : " + dto.getVehiculeId());
        }

        if (!"DISPONIBLE".equalsIgnoreCase(vehicule.getStatut())) {
            throw new BusinessException("Le vehicule n'est pas disponible pour la reservation (statut actuel : " + vehicule.getStatut() + ").");
        }

        // Verification des chevauchements de dates
        List<Location> activeRentals = repository.findActiveOrReservedByVehiculeId(dto.getVehiculeId());
        for (Location existing : activeRentals) {
            boolean overlap = !(dto.getDateFinPrevue().isBefore(existing.getDateDebut()) ||
                    dto.getDateDebut().isAfter(existing.getDateFinPrevue()));
            if (overlap) {
                throw new BusinessException("Le vehicule est deja reserve ou loue pour la periode demandee ("
                        + existing.getDateDebut() + " au " + existing.getDateFinPrevue() + ").");
            }
        }

        // 3. Calcul du prix avec remise fidélité
        long jours = ChronoUnit.DAYS.between(dto.getDateDebut(), dto.getDateFinPrevue());
        if (jours <= 0) jours = 1;

        BigDecimal dailyPrice = vehicule.getPrixJournalier();
        BigDecimal rawTotal = dailyPrice.multiply(BigDecimal.valueOf(jours));

        BigDecimal discountRate = getDiscountRate(client.getCategorieClient());
        BigDecimal discountAmount = rawTotal.multiply(discountRate);
        BigDecimal finalTotal = rawTotal.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        Location location = new Location();
        location.setClientId(dto.getClientId());
        location.setVehiculeId(dto.getVehiculeId());
        location.setDateDebut(dto.getDateDebut());
        location.setDateFinPrevue(dto.getDateFinPrevue());
        location.setKmDepart(vehicule.getKilometrage() != null ? vehicule.getKilometrage() : 0);
        location.setPrixJournalierApplique(dailyPrice);
        location.setMontantTotal(finalTotal);
        location.setPenalite(BigDecimal.ZERO);
        location.setStatut(StatutLocation.RESERVEE);

        Location saved = repository.save(location);
        log.info("Reservation creee avec succes : ID {}", saved.getId());
        return toDTO(saved);
    }

    public LocationSimulationResponseDTO simulatePrice(LocationSimulationRequestDTO req) {
        if (req.getDateFinPrevue().isBefore(req.getDateDebut())) {
            throw new BusinessException("La date de fin prevue doit etre posterieure ou egale a la date de debut.");
        }

        ClientRemoteDTO client = clientServiceClient.getClientById(req.getClientId());
        VehiculeRemoteDTO vehicule = vehiculeServiceClient.getVehiculeById(req.getVehiculeId());

        long jours = ChronoUnit.DAYS.between(req.getDateDebut(), req.getDateFinPrevue());
        if (jours <= 0) jours = 1;

        BigDecimal dailyPrice = vehicule.getPrixJournalier();
        BigDecimal rawTotal = dailyPrice.multiply(BigDecimal.valueOf(jours));

        BigDecimal discountRate = getDiscountRate(client.getCategorieClient());
        BigDecimal discountAmount = rawTotal.multiply(discountRate);
        BigDecimal finalTotal = rawTotal.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        LocationSimulationResponseDTO res = new LocationSimulationResponseDTO();
        res.setClientId(req.getClientId());
        res.setVehiculeId(req.getVehiculeId());
        res.setNombreJours(jours);
        res.setPrixJournalier(dailyPrice);
        res.setPrixBrut(rawTotal.setScale(2, RoundingMode.HALF_UP));
        res.setCategorieClient(client.getCategorieClient());
        res.setPourcentageRemise(discountRate.multiply(BigDecimal.valueOf(100)));
        res.setMontantRemise(discountAmount.setScale(2, RoundingMode.HALF_UP));
        res.setMontantTotal(finalTotal);

        return res;
    }

    public LocationDTO demarrerLocation(Long id) {
        Location location = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location introuvable avec l'identifiant : " + id));

        if (location.getStatut() != StatutLocation.RESERVEE) {
            throw new BusinessException("Impossible de demarrer une location qui n'est pas au statut RESERVEE.");
        }

        // Orchestration inter-services : passage du vehicule a LOUE
        try {
            vehiculeServiceClient.updateStatut(location.getVehiculeId(), Map.of("statut", "LOUE"));
        } catch (Exception e) {
            throw new BusinessException("Echec de la mise a jour du statut du vehicule : " + e.getMessage());
        }

        location.setStatut(StatutLocation.EN_COURS);
        Location updated = repository.save(location);
        log.info("Location {} demarree (EN_COURS)", id);
        return toDTO(updated);
    }

    public LocationDTO retournerVehicule(Long id, LocationRetourDTO dto) {
        Location location = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location introuvable avec l'identifiant : " + id));

        if (location.getStatut() != StatutLocation.EN_COURS) {
            throw new BusinessException("Seule une location EN_COURS peut etre marquee comme retournee.");
        }

        if (dto.getKmRetour() != null && location.getKmDepart() != null && dto.getKmRetour() < location.getKmDepart()) {
            throw new BusinessException("Le kilometrage de retour (" + dto.getKmRetour() +
                    ") ne peut pas etre inferieur au kilometrage de depart (" + location.getKmDepart() + ").");
        }

        LocalDate dateRetour = dto.getDateRetourEffectif() != null ? dto.getDateRetourEffectif() : LocalDate.now();
        location.setDateRetourEffectif(dateRetour);
        location.setKmRetour(dto.getKmRetour());

        // Calcul des penalites de retard : 1.5 * prixJournalier par jour de retard
        BigDecimal penalite = BigDecimal.ZERO;
        if (dateRetour.isAfter(location.getDateFinPrevue())) {
            long joursRetard = ChronoUnit.DAYS.between(location.getDateFinPrevue(), dateRetour);
            if (joursRetard > 0) {
                BigDecimal tarifJour = location.getPrixJournalierApplique();
                penalite = tarifJour.multiply(BigDecimal.valueOf(1.5))
                        .multiply(BigDecimal.valueOf(joursRetard))
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }

        location.setPenalite(penalite);
        location.setMontantTotal(location.getMontantTotal().add(penalite));
        location.setStatut(StatutLocation.TERMINEE);

        // Mise a jour du statut du vehicule a DISPONIBLE (inter-services)
        try {
            vehiculeServiceClient.updateStatut(location.getVehiculeId(), Map.of("statut", "DISPONIBLE"));
        } catch (Exception e) {
            log.error("Attention: Echec de mise a jour du vehicule {} a DISPONIBLE: {}", location.getVehiculeId(), e.getMessage());
        }

        Location saved = repository.save(location);

        // Mise a jour automatique de la categorie de fidelite du client
        checkAndUpdateFideliteClient(location.getClientId());

        return toDTO(saved);
    }

    public LocationDTO annulerLocation(Long id) {
        Location location = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location introuvable avec l'identifiant : " + id));

        if (location.getStatut() == StatutLocation.TERMINEE) {
            throw new BusinessException("Une location deja terminee ne peut pas etre annulee.");
        }

        if (location.getStatut() == StatutLocation.EN_COURS) {
            // Remettre le vehicule a DISPONIBLE en compensation
            try {
                vehiculeServiceClient.updateStatut(location.getVehiculeId(), Map.of("statut", "DISPONIBLE"));
            } catch (Exception e) {
                log.error("Erreur lors de la remise a disposition du vehicule lors de l'annulation : {}", e.getMessage());
            }
        }

        location.setStatut(StatutLocation.ANNULEE);
        return toDTO(repository.save(location));
    }

    public LocationStatistiquesDTO getStatistiques() {
        List<Location> all = repository.findAll();

        BigDecimal caTotal = BigDecimal.ZERO;
        BigDecimal caMois = BigDecimal.ZERO;
        LocalDate now = LocalDate.now();

        long enCours = 0;
        long terminees = 0;
        long reservees = 0;

        Map<Long, Long> countsByVehicule = new HashMap<>();
        Map<Long, BigDecimal> revenuByVehicule = new HashMap<>();

        for (Location loc : all) {
            if (loc.getStatut() == StatutLocation.EN_COURS) enCours++;
            else if (loc.getStatut() == StatutLocation.TERMINEE) terminees++;
            else if (loc.getStatut() == StatutLocation.RESERVEE) reservees++;

            if (loc.getStatut() == StatutLocation.TERMINEE || loc.getStatut() == StatutLocation.EN_COURS) {
                if (loc.getMontantTotal() != null) {
                    caTotal = caTotal.add(loc.getMontantTotal());
                    if (loc.getDateDebut() != null &&
                            loc.getDateDebut().getYear() == now.getYear() &&
                            loc.getDateDebut().getMonth() == now.getMonth()) {
                        caMois = caMois.add(loc.getMontantTotal());
                    }
                }
            }

            if (loc.getStatut() != StatutLocation.ANNULEE) {
                countsByVehicule.put(loc.getVehiculeId(), countsByVehicule.getOrDefault(loc.getVehiculeId(), 0L) + 1);
                revenuByVehicule.put(loc.getVehiculeId(),
                        revenuByVehicule.getOrDefault(loc.getVehiculeId(), BigDecimal.ZERO).add(
                                loc.getMontantTotal() != null ? loc.getMontantTotal() : BigDecimal.ZERO));
            }
        }

        List<LocationStatistiquesDTO.TopVehiculeItemDTO> topVehicules = countsByVehicule.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> new LocationStatistiquesDTO.TopVehiculeItemDTO(
                        e.getKey(),
                        e.getValue(),
                        revenuByVehicule.getOrDefault(e.getKey(), BigDecimal.ZERO)))
                .collect(Collectors.toList());

        LocationStatistiquesDTO stats = new LocationStatistiquesDTO();
        stats.setTotalLocations(all.size());
        stats.setLocationsEnCours(enCours);
        stats.setLocationsTerminees(terminees);
        stats.setLocationsReservees(reservees);
        stats.setChiffreAffairesTotal(caTotal.setScale(2, RoundingMode.HALF_UP));
        stats.setChiffreAffairesMois(caMois.setScale(2, RoundingMode.HALF_UP));

        // Taux d'occupation : pourcentage de vehicules en cours par rapport au total de locations actives
        double totalParcEstime = 7.0; // Parc fixe d'environ 7 vehicules
        double occupation = Math.min(100.0, (enCours / totalParcEstime) * 100.0);
        stats.setTauxOccupation(Math.round(occupation * 10.0) / 10.0);

        stats.setTopVehicules(topVehicules);
        return stats;
    }

    private void checkAndUpdateFideliteClient(Long clientId) {
        long completed = repository.countCompletedByClientId(clientId);
        log.info("Client {} possede {} locations terminees", clientId, completed);
        try {
            if (completed >= 15) {
                clientServiceClient.updateCategorie(clientId, Map.of("categorie", "VIP"));
                log.info("Client {} promu au statut VIP !", clientId);
            } else if (completed >= 5) {
                clientServiceClient.updateCategorie(clientId, Map.of("categorie", "FIDELE"));
                log.info("Client {} promu au statut FIDELE !", clientId);
            }
        } catch (Exception e) {
            log.warn("Echec de la mise a jour de fidelite pour client {} : {}", clientId, e.getMessage());
        }
    }

    private BigDecimal getDiscountRate(String categorie) {
        if ("VIP".equalsIgnoreCase(categorie)) {
            return new BigDecimal("0.10");
        } else if ("FIDELE".equalsIgnoreCase(categorie)) {
            return new BigDecimal("0.05");
        }
        return BigDecimal.ZERO;
    }

    private LocationDTO toDTO(Location l) {
        if (l == null) return null;
        LocationDTO dto = new LocationDTO();
        mapCommonFields(l, dto);
        return dto;
    }

    private void mapCommonFields(Location src, LocationDTO dest) {
        dest.setId(src.getId());
        dest.setClientId(src.getClientId());
        dest.setVehiculeId(src.getVehiculeId());
        dest.setDateDebut(src.getDateDebut());
        dest.setDateFinPrevue(src.getDateFinPrevue());
        dest.setDateRetourEffectif(src.getDateRetourEffectif());
        dest.setKmDepart(src.getKmDepart());
        dest.setKmRetour(src.getKmRetour());
        dest.setPrixJournalierApplique(src.getPrixJournalierApplique());
        dest.setMontantTotal(src.getMontantTotal());
        dest.setPenalite(src.getPenalite());
        dest.setStatut(src.getStatut());
        dest.setDateCreation(src.getDateCreation());
    }

    private void mapCommonFields(Location src, LocationEnrichieDTO dest) {
        dest.setId(src.getId());
        dest.setClientId(src.getClientId());
        dest.setVehiculeId(src.getVehiculeId());
        dest.setDateDebut(src.getDateDebut());
        dest.setDateFinPrevue(src.getDateFinPrevue());
        dest.setDateRetourEffectif(src.getDateRetourEffectif());
        dest.setKmDepart(src.getKmDepart());
        dest.setKmRetour(src.getKmRetour());
        dest.setPrixJournalierApplique(src.getPrixJournalierApplique());
        dest.setMontantTotal(src.getMontantTotal());
        dest.setPenalite(src.getPenalite());
        dest.setStatut(src.getStatut());
        dest.setDateCreation(src.getDateCreation());
    }
}
