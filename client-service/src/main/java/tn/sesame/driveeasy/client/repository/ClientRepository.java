package tn.sesame.driveeasy.client.repository;

import tn.sesame.driveeasy.client.model.Client;

import java.util.List;
import java.util.Optional;

public interface ClientRepository {
    List<Client> findAll();
    Optional<Client> findById(Long id);
    List<Client> search(String query);
    Optional<Client> findByCin(String cin);
    Optional<Client> findByEmail(String email);
    Optional<Client> findByNumeroPermis(String numeroPermis);
    Client save(Client client);
    boolean deleteById(Long id);
}
