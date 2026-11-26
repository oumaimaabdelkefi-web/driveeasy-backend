package tn.sesame.driveeasy.client.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import tn.sesame.driveeasy.client.model.CategorieClient;
import tn.sesame.driveeasy.client.model.Client;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Implementation en memoire vive (RAM) du repository Client.
 * Conforme a l'exigence : bases de donnees strictement interdites.
 */
@Repository
public class InMemoryClientRepository implements ClientRepository {

    private final Map<Long, Client> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @PostConstruct
    public void initDemoData() {
        save(new Client(null, "09876543", "Mansour", "Amine",
                LocalDate.of(1996, 5, 14), "amine.mansour@gmail.com", "+216 22 110 330",
                "15 Rue de Carthage, Tunis", "P-TN-102938",
                LocalDate.of(2030, 8, 20), CategorieClient.FIDELE, LocalDate.of(2023, 1, 10)));

        save(new Client(null, "12345678", "Ben Ali", "Sarra",
                LocalDate.of(1990, 11, 23), "sarra.benali@outlook.com", "+216 55 440 220",
                "8 Avenue Habib Bourguiba, Ariana", "P-TN-554433",
                LocalDate.of(2029, 4, 15), CategorieClient.VIP, LocalDate.of(2022, 6, 5)));

        save(new Client(null, "08765432", "Trabelsi", "Karim",
                LocalDate.of(2000, 3, 10), "karim.trabelsi@yahoo.com", "+216 98 765 432",
                "45 Rue de la Republique, Sousse", "P-TN-778899",
                LocalDate.of(2028, 12, 10), CategorieClient.STANDARD, LocalDate.of(2024, 2, 18)));

        // Client non eligible : moins de 21 ans (ne en 2006)
        save(new Client(null, "07654321", "Jaziri", "Nour",
                LocalDate.of(2006, 9, 15), "nour.jaziri@gmail.com", "+216 29 888 111",
                "12 Rue des Oliviers, Nabeul", "P-TN-990011",
                LocalDate.of(2031, 3, 25), CategorieClient.STANDARD, LocalDate.of(2024, 5, 12)));

        // Client non eligible : permis expire (expire en 2023)
        save(new Client(null, "05432109", "Dridi", "Mohamed",
                LocalDate.of(1984, 7, 8), "mohamed.dridi@gmail.com", "+216 21 000 999",
                "3 Avenue des Roses, Bizerte", "P-TN-112233",
                LocalDate.of(2023, 1, 1), CategorieClient.STANDARD, LocalDate.of(2021, 9, 1)));
    }

    @Override
    public List<Client> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Client> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Client> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        String q = query.toLowerCase().trim();
        return storage.values().stream()
                .filter(c -> (c.getNom() != null && c.getNom().toLowerCase().contains(q)) ||
                        (c.getPrenom() != null && c.getPrenom().toLowerCase().contains(q)) ||
                        (c.getCin() != null && c.getCin().toLowerCase().contains(q)) ||
                        (c.getEmail() != null && c.getEmail().toLowerCase().contains(q)))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Client> findByCin(String cin) {
        if (cin == null) return Optional.empty();
        return storage.values().stream()
                .filter(c -> c.getCin() != null && c.getCin().equalsIgnoreCase(cin.trim()))
                .findFirst();
    }

    @Override
    public Optional<Client> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return storage.values().stream()
                .filter(c -> c.getEmail() != null && c.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst();
    }

    @Override
    public Optional<Client> findByNumeroPermis(String numeroPermis) {
        if (numeroPermis == null) return Optional.empty();
        return storage.values().stream()
                .filter(c -> c.getNumeroPermis() != null && c.getNumeroPermis().equalsIgnoreCase(numeroPermis.trim()))
                .findFirst();
    }

    @Override
    public synchronized Client save(Client client) {
        if (client.getId() == null) {
            client.setId(idSequence.getAndIncrement());
        }
        if (client.getDateInscription() == null) {
            client.setDateInscription(LocalDate.now());
        }
        storage.put(client.getId(), client);
        return client;
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) return false;
        return storage.remove(id) != null;
    }
}
