package tn.sesame.driveeasy.vehicule.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tn.sesame.driveeasy.vehicule.model.BoiteVitesse;
import tn.sesame.driveeasy.vehicule.model.Carburant;
import tn.sesame.driveeasy.vehicule.model.StatutVehicule;

import java.math.BigDecimal;

public class VehiculeDTO {

    private Long id;

    @NotBlank(message = "L'immatriculation est obligatoire")
    private String immatriculation;

    @NotBlank(message = "La marque est obligatoire")
    private String marque;

    @NotBlank(message = "Le modele est obligatoire")
    private String modele;

    @NotNull(message = "L'annee est obligatoire")
    @Min(value = 1990, message = "L'annee doit etre superieure ou egale a 1990")
    private Integer annee;

    @NotBlank(message = "La couleur est obligatoire")
    private String couleur;

    @NotNull(message = "Le type de carburant est obligatoire")
    private Carburant carburant;

    @NotNull(message = "Le type de boite de vitesses est obligatoire")
    private BoiteVitesse boite;

    @NotNull(message = "Le nombre de places est obligatoire")
    @Min(value = 1, message = "Le nombre de places doit etre d'au moins 1")
    private Integer nbPlaces;

    @NotNull(message = "Le kilometrage est obligatoire")
    @Min(value = 0, message = "Le kilometrage ne peut pas etre negatif")
    private Integer kilometrage;

    @NotNull(message = "Le prix journalier est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix journalier doit etre strictement positif")
    private BigDecimal prixJournalier;

    private StatutVehicule statut = StatutVehicule.DISPONIBLE;

    private String imageUrl;

    public VehiculeDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public Integer getAnnee() {
        return annee;
    }

    public void setAnnee(Integer annee) {
        this.annee = annee;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public Carburant getCarburant() {
        return carburant;
    }

    public void setCarburant(Carburant carburant) {
        this.carburant = carburant;
    }

    public BoiteVitesse getBoite() {
        return boite;
    }

    public void setBoite(BoiteVitesse boite) {
        this.boite = boite;
    }

    public Integer getNbPlaces() {
        return nbPlaces;
    }

    public void setNbPlaces(Integer nbPlaces) {
        this.nbPlaces = nbPlaces;
    }

    public Integer getKilometrage() {
        return kilometrage;
    }

    public void setKilometrage(Integer kilometrage) {
        this.kilometrage = kilometrage;
    }

    public BigDecimal getPrixJournalier() {
        return prixJournalier;
    }

    public void setPrixJournalier(BigDecimal prixJournalier) {
        this.prixJournalier = prixJournalier;
    }

    public StatutVehicule getStatut() {
        return statut;
    }

    public void setStatut(StatutVehicule statut) {
        this.statut = statut;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
