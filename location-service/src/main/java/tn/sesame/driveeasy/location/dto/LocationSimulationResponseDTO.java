package tn.sesame.driveeasy.location.dto;

import java.math.BigDecimal;

public class LocationSimulationResponseDTO {

    private Long clientId;
    private Long vehiculeId;
    private long nombreJours;
    private BigDecimal prixJournalier;
    private BigDecimal prixBrut;
    private String categorieClient;
    private BigDecimal pourcentageRemise;
    private BigDecimal montantRemise;
    private BigDecimal montantTotal;

    public LocationSimulationResponseDTO() {
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

    public long getNombreJours() {
        return nombreJours;
    }

    public void setNombreJours(long nombreJours) {
        this.nombreJours = nombreJours;
    }

    public BigDecimal getPrixJournalier() {
        return prixJournalier;
    }

    public void setPrixJournalier(BigDecimal prixJournalier) {
        this.prixJournalier = prixJournalier;
    }

    public BigDecimal getPrixBrut() {
        return prixBrut;
    }

    public void setPrixBrut(BigDecimal prixBrut) {
        this.prixBrut = prixBrut;
    }

    public String getCategorieClient() {
        return categorieClient;
    }

    public void setCategorieClient(String categorieClient) {
        this.categorieClient = categorieClient;
    }

    public BigDecimal getPourcentageRemise() {
        return pourcentageRemise;
    }

    public void setPourcentageRemise(BigDecimal pourcentageRemise) {
        this.pourcentageRemise = pourcentageRemise;
    }

    public BigDecimal getMontantRemise() {
        return montantRemise;
    }

    public void setMontantRemise(BigDecimal montantRemise) {
        this.montantRemise = montantRemise;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }
}
