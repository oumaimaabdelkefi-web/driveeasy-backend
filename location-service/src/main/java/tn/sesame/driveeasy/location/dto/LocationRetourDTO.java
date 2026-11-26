package tn.sesame.driveeasy.location.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class LocationRetourDTO {

    @NotNull(message = "Le kilometrage au retour est obligatoire")
    private Integer kmRetour;

    private LocalDate dateRetourEffectif;

    public LocationRetourDTO() {
    }

    public LocationRetourDTO(Integer kmRetour, LocalDate dateRetourEffectif) {
        this.kmRetour = kmRetour;
        this.dateRetourEffectif = dateRetourEffectif;
    }

    public Integer getKmRetour() {
        return kmRetour;
    }

    public void setKmRetour(Integer kmRetour) {
        this.kmRetour = kmRetour;
    }

    public LocalDate getDateRetourEffectif() {
        return dateRetourEffectif;
    }

    public void setDateRetourEffectif(LocalDate dateRetourEffectif) {
        this.dateRetourEffectif = dateRetourEffectif;
    }
}
