package com.eventpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneReservationResponse {
    private Long id;
    private Long categorieId;
    private String categorieNom;
    private int quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal sousTotal;
}
