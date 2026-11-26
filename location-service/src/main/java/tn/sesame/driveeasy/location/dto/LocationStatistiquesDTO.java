package tn.sesame.driveeasy.location.dto;

import java.math.BigDecimal;
import java.util.List;

public class LocationStatistiquesDTO {

    private BigDecimal chiffreAffairesTotal;
    private BigDecimal chiffreAffairesMois;
    private long totalLocations;
    private long locationsEnCours;
    private long locationsTerminees;
    private long locationsReservees;
    private double tauxOccupation;
    private List<TopVehiculeItemDTO> topVehicules;

    public LocationStatistiquesDTO() {
    }

    public BigDecimal getChiffreAffairesTotal() {
        return chiffreAffairesTotal;
    }

    public void setChiffreAffairesTotal(BigDecimal chiffreAffairesTotal) {
        this.chiffreAffairesTotal = chiffreAffairesTotal;
    }

    public BigDecimal getChiffreAffairesMois() {
        return chiffreAffairesMois;
    }

    public void setChiffreAffairesMois(BigDecimal chiffreAffairesMois) {
        this.chiffreAffairesMois = chiffreAffairesMois;
    }

    public long getTotalLocations() {
        return totalLocations;
    }

    public void setTotalLocations(long totalLocations) {
        this.totalLocations = totalLocations;
    }

    public long getLocationsEnCours() {
        return locationsEnCours;
    }

    public void setLocationsEnCours(long locationsEnCours) {
        this.locationsEnCours = locationsEnCours;
    }

    public long getLocationsTerminees() {
        return locationsTerminees;
    }

    public void setLocationsTerminees(long locationsTerminees) {
        this.locationsTerminees = locationsTerminees;
    }

    public long getLocationsReservees() {
        return locationsReservees;
    }

    public void setLocationsReservees(long locationsReservees) {
        this.locationsReservees = locationsReservees;
    }

    public double getTauxOccupation() {
        return tauxOccupation;
    }

    public void setTauxOccupation(double tauxOccupation) {
        this.tauxOccupation = tauxOccupation;
    }

    public List<TopVehiculeItemDTO> getTopVehicules() {
        return topVehicules;
    }

    public void setTopVehicules(List<TopVehiculeItemDTO> topVehicules) {
        this.topVehicules = topVehicules;
    }

    public static class TopVehiculeItemDTO {
        private Long vehiculeId;
        private long nombreLocations;
        private BigDecimal totalRevenu;

        public TopVehiculeItemDTO() {
        }

        public TopVehiculeItemDTO(Long vehiculeId, long nombreLocations, BigDecimal totalRevenu) {
            this.vehiculeId = vehiculeId;
            this.nombreLocations = nombreLocations;
            this.totalRevenu = totalRevenu;
        }

        public Long getVehiculeId() {
            return vehiculeId;
        }

        public void setVehiculeId(Long vehiculeId) {
            this.vehiculeId = vehiculeId;
        }

        public long getNombreLocations() {
            return nombreLocations;
        }

        public void setNombreLocations(long nombreLocations) {
            this.nombreLocations = nombreLocations;
        }

        public BigDecimal getTotalRevenu() {
            return totalRevenu;
        }

        public void setTotalRevenu(BigDecimal totalRevenu) {
            this.totalRevenu = totalRevenu;
        }
    }
}
