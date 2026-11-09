package com.eventpro.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class CategoryUpdateRequest {

    private String nom;

    @DecimalMin(value = "0.0", inclusive = true, message = "Le prix doit être positif ou nul")
    private BigDecimal prixUnitaire;

    @Min(value = 1, message = "La quantité totale doit être d'au moins 1 place")
    private Integer quantiteTotale;

    @Min(value = 1, message = "Le quota max par client doit être d'au moins 1 place")
    private Integer quotaMaxParClient;
}
