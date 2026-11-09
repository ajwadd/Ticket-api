package com.eventpro.dto.request;

import jakarta.validation.constraints.DecimalMin;
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
public class PaymentRequest {

    @NotBlank(message = "La référence de la réservation est obligatoire")
    private String reservationReference;

    @NotNull(message = "Le montant du paiement est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le montant doit être supérieur ou égal à 0")
    private BigDecimal montant;
}
