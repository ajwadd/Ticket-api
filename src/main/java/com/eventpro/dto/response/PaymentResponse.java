package com.eventpro.dto.response;

import com.eventpro.enums.StatutPaiement;
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
public class PaymentResponse {
    private Long id;
    private String reference;
    private String reservationReference;
    private BigDecimal montant;
    private LocalDateTime datePaiement;
    private StatutPaiement statut;
    private List<TicketResponse> billets;
}
