package tn.sesame.driveeasy.location.repository;

import tn.sesame.driveeasy.location.model.Location;
import tn.sesame.driveeasy.location.model.StatutLocation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LocationRepository {
    List<Location> findAll();
    Optional<Location> findById(Long id);
    List<Location> findByFilters(StatutLocation statut, Long clientId, Long vehiculeId, LocalDate dateDebut, LocalDate dateFin);
    long countCompletedByClientId(Long clientId);
    List<Location> findActiveOrReservedByVehiculeId(Long vehiculeId);
    Location save(Location location);
    boolean deleteById(Long id);
}
