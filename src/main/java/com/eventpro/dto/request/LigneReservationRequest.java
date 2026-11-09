package com.eventpro.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneReservationRequest {

    @NotNull(message = "L'identifiant de la catégorie est obligatoire")
    private Long categorieId;

    @Min(value = 1, message = "La quantité doit être supérieure ou égale à 1")
    private int quantite;

    private String nomPorteur;
}
