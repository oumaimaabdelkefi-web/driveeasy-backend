package tn.sesame.driveeasy.location.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import tn.sesame.driveeasy.location.model.Location;
import tn.sesame.driveeasy.location.model.StatutLocation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Implementation en memoire vive (RAM) du repository Location.
 * Conforme a l'exigence : bases de donnees strictement interdites.
 */
@Repository
public class InMemoryLocationRepository implements LocationRepository {

    private final Map<Long, Location> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @PostConstruct
    public void initDemoData() {
        LocalDate today = LocalDate.now();

        // 1. Location en cours pour Vehicule 3 (Golf 8) et Client 1 (Amine Mansour)
        save(new Location(null, 1L, 3L, today.minusDays(2), today.plusDays(3),
                null, 41000, null, new BigDecimal("120.00"),
                new BigDecimal("570.00"), BigDecimal.ZERO, StatutLocation.EN_COURS,
                LocalDateTime.now().minusDays(2)));

        // 2. Location terminee pour Vehicule 1 (Peugeot 208) et Client 2 (Sarra Ben Ali - VIP)
        save(new Location(null, 2L, 1L, today.minusDays(10), today.minusDays(5),
                today.minusDays(5), 25000, 25400, new BigDecimal("85.00"),
                new BigDecimal("459.00"), BigDecimal.ZERO, StatutLocation.TERMINEE,
                LocalDateTime.now().minusDays(10)));

        // 3. Location reservee pour Vehicule 2 (Clio 5) et Client 3 (Karim Trabelsi)
        save(new Location(null, 3L, 2L, today.plusDays(1), today.plusDays(4),
                null, 14200, null, new BigDecimal("80.00"),
                new BigDecimal("320.00"), BigDecimal.ZERO, StatutLocation.RESERVEE,
                LocalDateTime.now().minusHours(3)));

        // 4. Locations passees pour simuler l'historique et la fidelite du client 1 (Amine Mansour)
        for (int i = 1; i <= 4; i++) {
            save(new Location(null, 1L, 2L, today.minusMonths(i).minusDays(3), today.minusMonths(i),
                    today.minusMonths(i), 10000 + i * 500, 10300 + i * 500, new BigDecimal("80.00"),
                    new BigDecimal("304.00"), BigDecimal.ZERO, StatutLocation.TERMINEE,
                    LocalDateTime.now().minusMonths(i).minusDays(3)));
        }
    }

    @Override
    public List<Location> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Location> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Location> findByFilters(StatutLocation statut, Long clientId, Long vehiculeId, LocalDate dateDebut, LocalDate dateFin) {
        return storage.values().stream()
                .filter(l -> statut == null || l.getStatut() == statut)
                .filter(l -> clientId == null || l.getClientId().equals(clientId))
                .filter(l -> vehiculeId == null || l.getVehiculeId().equals(vehiculeId))
                .filter(l -> dateDebut == null || !l.getDateDebut().isBefore(dateDebut))
                .filter(l -> dateFin == null || !l.getDateFinPrevue().isAfter(dateFin))
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public long countCompletedByClientId(Long clientId) {
        if (clientId == null) return 0;
        return storage.values().stream()
                .filter(l -> clientId.equals(l.getClientId()) && l.getStatut() == StatutLocation.TERMINEE)
                .count();
    }

    @Override
    public List<Location> findActiveOrReservedByVehiculeId(Long vehiculeId) {
        if (vehiculeId == null) return List.of();
        return storage.values().stream()
                .filter(l -> vehiculeId.equals(l.getVehiculeId()) &&
                        (l.getStatut() == StatutLocation.RESERVEE || l.getStatut() == StatutLocation.EN_COURS))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized Location save(Location location) {
        if (location.getId() == null) {
            location.setId(idSequence.getAndIncrement());
        }
        if (location.getDateCreation() == null) {
            location.setDateCreation(LocalDateTime.now());
        }
        storage.put(location.getId(), location);
        return location;
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) return false;
        return storage.remove(id) != null;
    }
}
