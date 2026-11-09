package com.eventpro.controller;

import com.eventpro.dto.request.PaymentRequest;
import com.eventpro.dto.response.PaymentResponse;
import com.eventpro.service.PaiementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Paiements", description = "Règlement des réservations avec contrôle strict d'idempotence et émission des billets")
public class PaiementController {

    private final PaiementService paiementService;

    @PostMapping
    @Operation(summary = "Confirmer un paiement de réservation de manière idempotente et émettre les billets")
    public ResponseEntity<PaymentResponse> effectuerPaiement(
            @Parameter(description = "Clé unique d'idempotence pour prévenir les doubles débits", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paiementService.effectuerPaiement(idempotencyKey, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reference}")
    @Operation(summary = "Consulter le détail d'un paiement par sa référence")
    public ResponseEntity<PaymentResponse> getPaiementByReference(@PathVariable String reference) {
        return ResponseEntity.ok(paiementService.getPaiementByReference(reference));
    }

    @GetMapping("/reservation/{reservationReference}")
    @Operation(summary = "Consulter le paiement associé à une réservation")
    public ResponseEntity<PaymentResponse> getPaiementByReservation(@PathVariable String reservationReference) {
        return ResponseEntity.ok(paiementService.getPaiementByReservation(reservationReference));
    }
}
