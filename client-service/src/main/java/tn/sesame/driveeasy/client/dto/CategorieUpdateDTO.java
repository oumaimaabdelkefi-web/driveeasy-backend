package tn.sesame.driveeasy.client.dto;

import jakarta.validation.constraints.NotNull;
import tn.sesame.driveeasy.client.model.CategorieClient;

public class CategorieUpdateDTO {

    @NotNull(message = "La categorie est obligatoire")
    private CategorieClient categorie;

    public CategorieUpdateDTO() {
    }

    public CategorieUpdateDTO(CategorieClient categorie) {
        this.categorie = categorie;
    }

    public CategorieClient getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieClient categorie) {
        this.categorie = categorie;
    }
}
