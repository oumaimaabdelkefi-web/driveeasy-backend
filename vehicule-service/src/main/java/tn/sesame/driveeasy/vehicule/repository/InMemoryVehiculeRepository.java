package tn.sesame.driveeasy.vehicule.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import tn.sesame.driveeasy.vehicule.model.BoiteVitesse;
import tn.sesame.driveeasy.vehicule.model.Carburant;
import tn.sesame.driveeasy.vehicule.model.StatutVehicule;
import tn.sesame.driveeasy.vehicule.model.Vehicule;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Implementation en memoire vive (RAM) du repository de Vehicules.
 * Conforme a la regle : bases de donnees strictement interdites.
 */
@Repository
public class InMemoryVehiculeRepository implements VehiculeRepository {

    private final Map<Long, Vehicule> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @PostConstruct
    public void initDemoData() {
        save(new Vehicule(null, "220-TN-1001", "Peugeot", "208", 2023, "Blanc Nacre",
                Carburant.ESSENCE, BoiteVitesse.MANUELLE, 5, 25400,
                new BigDecimal("85.00"), StatutVehicule.DISPONIBLE,
                "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80"));

        save(new Vehicule(null, "225-TN-2002", "Renault", "Clio 5", 2024, "Rouge Flamme",
                Carburant.ESSENCE, BoiteVitesse.MANUELLE, 5, 14200,
                new BigDecimal("80.00"), StatutVehicule.DISPONIBLE,
                "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?auto=format&fit=crop&w=800&q=80"));

        save(new Vehicule(null, "218-TN-3003", "Volkswagen", "Golf 8", 2023, "Gris Dauphin",
                Carburant.DIESEL, BoiteVitesse.AUTOMATIQUE, 5, 41000,
                new BigDecimal("120.00"), StatutVehicule.LOUE,
                "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80"));

        save(new Vehicule(null, "230-TN-4004", "Toyota", "Yaris Cross", 2024, "Bleu Cobalt",
                Carburant.HYBRIDE, BoiteVitesse.AUTOMATIQUE, 5, 18500,
                new BigDecimal("110.00"), StatutVehicule.DISPONIBLE,
                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80"));

        save(new Vehicule(null, "235-TN-5005", "Tesla", "Model 3", 2024, "Noir Pur",
                Carburant.ELECTRIQUE, BoiteVitesse.AUTOMATIQUE, 5, 9300,
                new BigDecimal("160.00"), StatutVehicule.DISPONIBLE,
                "https://images.unsplash.com/photo-1560958089-b8a1929cea89?auto=format&fit=crop&w=800&q=80"));

        save(new Vehicule(null, "210-TN-6006", "Hyundai", "Tucson", 2022, "Gris Sideral",
                Carburant.DIESEL, BoiteVitesse.AUTOMATIQUE, 5, 68000,
                new BigDecimal("130.00"), StatutVehicule.MAINTENANCE,
                "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80"));

        save(new Vehicule(null, "240-TN-7007", "Mercedes-Benz", "Classe A", 2024, "Argent Iridium",
                Carburant.ESSENCE, BoiteVitesse.AUTOMATIQUE, 5, 12000,
                new BigDecimal("180.00"), StatutVehicule.DISPONIBLE,
                "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?auto=format&fit=crop&w=800&q=80"));
    }

    @Override
    public List<Vehicule> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Vehicule> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Vehicule> findByImmatriculation(String immatriculation) {
        if (immatriculation == null) return Optional.empty();
        return storage.values().stream()
                .filter(v -> v.getImmatriculation() != null && v.getImmatriculation().equalsIgnoreCase(immatriculation.trim()))
                .findFirst();
    }

    @Override
    public List<Vehicule> findByFilters(String marque, Carburant carburant, StatutVehicule statut, BigDecimal prixMax) {
        return storage.values().stream()
                .filter(v -> marque == null || marque.isBlank() ||
                        (v.getMarque() != null && v.getMarque().toLowerCase().contains(marque.toLowerCase().trim())) ||
                        (v.getModele() != null && v.getModele().toLowerCase().contains(marque.toLowerCase().trim())))
                .filter(v -> carburant == null || v.getCarburant() == carburant)
                .filter(v -> statut == null || v.getStatut() == statut)
                .filter(v -> prixMax == null || (v.getPrixJournalier() != null && v.getPrixJournalier().compareTo(prixMax) <= 0))
                .collect(Collectors.toList());
    }

    @Override
    public List<Vehicule> findAvailable() {
        return storage.values().stream()
                .filter(v -> v.getStatut() == StatutVehicule.DISPONIBLE)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized Vehicule save(Vehicule vehicule) {
        if (vehicule.getId() == null) {
            vehicule.setId(idSequence.getAndIncrement());
        }
        storage.put(vehicule.getId(), vehicule);
        return vehicule;
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) return false;
        return storage.remove(id) != null;
    }

    @Override
    public boolean existsByImmatriculation(String immatriculation) {
        return findByImmatriculation(immatriculation).isPresent();
    }
}
