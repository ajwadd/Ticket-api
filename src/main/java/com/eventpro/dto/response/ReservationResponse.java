package com.eventpro.dto.response;

import com.eventpro.enums.StatutReservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long id;
    private String reference;
    private Long evenementId;
    private String titreEvenement;
    private Long clientId;
    private String clientNom;
    private LocalDateTime dateCreation;
    private LocalDateTime dateExpiration;
    private StatutReservation statut;
    private BigDecimal montantBrut;
    private BigDecimal montantReduction;
    private BigDecimal montantNet;
    private String codePromoApplique;
    private List<LigneReservationResponse> lignes;
}
