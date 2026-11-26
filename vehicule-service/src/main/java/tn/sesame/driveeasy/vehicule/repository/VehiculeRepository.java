package tn.sesame.driveeasy.vehicule.repository;

import tn.sesame.driveeasy.vehicule.model.Carburant;
import tn.sesame.driveeasy.vehicule.model.StatutVehicule;
import tn.sesame.driveeasy.vehicule.model.Vehicule;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VehiculeRepository {
    List<Vehicule> findAll();
    Optional<Vehicule> findById(Long id);
    Optional<Vehicule> findByImmatriculation(String immatriculation);
    List<Vehicule> findByFilters(String marque, Carburant carburant, StatutVehicule statut, BigDecimal prixMax);
    List<Vehicule> findAvailable();
    Vehicule save(Vehicule vehicule);
    boolean deleteById(Long id);
    boolean existsByImmatriculation(String immatriculation);
}
