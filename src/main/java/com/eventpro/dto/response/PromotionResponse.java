package com.eventpro.dto.response;

import com.eventpro.enums.TypeReduction;
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
public class PromotionResponse {
    private Long id;
    private String code;
    private TypeReduction typeReduction;
    private BigDecimal valeurReduction;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private int limiteUtilisation;
    private int nombreUtilisations;
    private int quotaDisponible;
    private Long evenementId;
}
