package com.eventpro.dto.request;

import com.eventpro.enums.TypeReduction;
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
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionCreateRequest {

    @NotBlank(message = "Le code promotionnel est obligatoire")
    private String code;

    @NotNull(message = "Le type de réduction est obligatoire")
    private TypeReduction typeReduction;

    @NotNull(message = "La valeur de la réduction est obligatoire")
    @DecimalMin(value = "0.01", message = "La réduction doit être strictement positive")
    private BigDecimal valeurReduction;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime dateFin;

    @Min(value = 1, message = "La limite d'utilisation doit être d'au moins 1")
    private int limiteUtilisation;

    private Long evenementId;

    private Long organisateurId;
}
