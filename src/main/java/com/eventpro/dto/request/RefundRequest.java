package com.eventpro.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class RefundRequest {

    @NotBlank(message = "La référence du paiement est obligatoire")
    private String paiementReference;

    @NotBlank(message = "Le motif du remboursement est obligatoire")
    private String motif;
}
