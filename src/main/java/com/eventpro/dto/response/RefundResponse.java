package com.eventpro.dto.response;

import com.eventpro.enums.StatutRemboursement;
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
public class RefundResponse {
    private Long id;
    private String paiementReference;
    private BigDecimal montantPaye;
    private BigDecimal montantRembourse;
    private int pourcentageRembourse;
    private int pourcentageFraisRetenus;
    private String motif;
    private StatutRemboursement statut;
    private LocalDateTime dateDemande;
    private String message;
}
