package tn.sesame.driveeasy.location.dto;

import tn.sesame.driveeasy.location.client.ClientRemoteDTO;
import tn.sesame.driveeasy.location.client.VehiculeRemoteDTO;
import tn.sesame.driveeasy.location.model.StatutLocation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LocationEnrichieDTO {

    private Long id;
    private Long clientId;
    private Long vehiculeId;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;
    private LocalDate dateRetourEffectif;
    private Integer kmDepart;
    private Integer kmRetour;
    private BigDecimal prixJournalierApplique;
    private BigDecimal montantTotal;
    private BigDecimal penalite;
    private StatutLocation statut;
    private LocalDateTime dateCreation;

    private ClientRemoteDTO client;
    private VehiculeRemoteDTO vehicule;

    public LocationEnrichieDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(Long vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFinPrevue() {
        return dateFinPrevue;
    }

    public void setDateFinPrevue(LocalDate dateFinPrevue) {
        this.dateFinPrevue = dateFinPrevue;
    }

    public LocalDate getDateRetourEffectif() {
        return dateRetourEffectif;
    }

    public void setDateRetourEffectif(LocalDate dateRetourEffectif) {
        this.dateRetourEffectif = dateRetourEffectif;
    }

    public Integer getKmDepart() {
        return kmDepart;
    }

    public void setKmDepart(Integer kmDepart) {
        this.kmDepart = kmDepart;
    }

    public Integer getKmRetour() {
        return kmRetour;
    }

    public void setKmRetour(Integer kmRetour) {
        this.kmRetour = kmRetour;
    }

    public BigDecimal getPrixJournalierApplique() {
        return prixJournalierApplique;
    }

    public void setPrixJournalierApplique(BigDecimal prixJournalierApplique) {
        this.prixJournalierApplique = prixJournalierApplique;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public BigDecimal getPenalite() {
        return penalite;
    }

    public void setPenalite(BigDecimal penalite) {
        this.penalite = penalite;
    }

    public StatutLocation getStatut() {
        return statut;
    }

    public void setStatut(StatutLocation statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public ClientRemoteDTO getClient() {
        return client;
    }

    public void setClient(ClientRemoteDTO client) {
        this.client = client;
    }

    public VehiculeRemoteDTO getVehicule() {
        return vehicule;
    }

    public void setVehicule(VehiculeRemoteDTO vehicule) {
        this.vehicule = vehicule;
    }
}
