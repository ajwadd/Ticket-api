package com.eventpro.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CategoryCreateRequest {

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    private String nom;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le prix doit être positif ou nul")
    private BigDecimal prixUnitaire;

    @Min(value = 1, message = "La quantité totale doit être d'au moins 1 place")
    private int quantiteTotale;

    @Min(value = 1, message = "Le quota max par client doit être d'au moins 1 place")
    @Builder.Default
    private int quotaMaxParClient = 10;
}
