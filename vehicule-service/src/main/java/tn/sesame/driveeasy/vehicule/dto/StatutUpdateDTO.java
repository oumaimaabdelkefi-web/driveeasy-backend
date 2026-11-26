package tn.sesame.driveeasy.vehicule.dto;

import jakarta.validation.constraints.NotNull;
import tn.sesame.driveeasy.vehicule.model.StatutVehicule;

public class StatutUpdateDTO {

    @NotNull(message = "Le statut est obligatoire")
    private StatutVehicule statut;

    public StatutUpdateDTO() {
    }

    public StatutUpdateDTO(StatutVehicule statut) {
        this.statut = statut;
    }

    public StatutVehicule getStatut() {
        return statut;
    }

    public void setStatut(StatutVehicule statut) {
        this.statut = statut;
    }
}
