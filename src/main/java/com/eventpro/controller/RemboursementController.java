package com.eventpro.controller;

import com.eventpro.dto.request.RefundRequest;
import com.eventpro.dto.response.RefundResponse;
import com.eventpro.service.RemboursementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
@Tag(name = "Remboursements", description = "Demandes et gestion des remboursements selon les barèmes stricts")
public class RemboursementController {

    private final RemboursementService remboursementService;

    @PostMapping
    @Operation(summary = "Initier une demande d'annulation et de remboursement (barème selon délai restant)")
    public ResponseEntity<RefundResponse> demanderRemboursement(@Valid @RequestBody RefundRequest request) {
        RefundResponse response = remboursementService.traiterDemandeRemboursement(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payment/{paiementId}")
    @Operation(summary = "Consulter les remboursements liés à un paiement")
    public ResponseEntity<List<RefundResponse>> getRemboursementsParPaiement(@PathVariable Long paiementId) {
        return ResponseEntity.ok(remboursementService.getRemboursementsParPaiement(paiementId));
    }
}
