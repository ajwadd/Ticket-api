package com.eventpro.controller;

import com.eventpro.dto.request.ReservationRequest;
import com.eventpro.dto.response.ReservationResponse;
import com.eventpro.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Réservations", description = "Gestion des réservations temporaires (10 minutes) avec verrous pessimistes")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Créer une réservation temporaire et bloquer les places pendant 10 minutes sous verrou pessimiste")
    public ResponseEntity<ReservationResponse> creerReservation(@Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.creerReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{reference}")
    @Operation(summary = "Consulter une réservation par sa référence unique")
    public ResponseEntity<ReservationResponse> getReservationByReference(@PathVariable String reference) {
        return ResponseEntity.ok(reservationService.getReservationByReference(reference));
    }

    @DeleteMapping("/{reference}")
    @Operation(summary = "Annuler volontairement une réservation en attente et libérer immédiatement les places")
    public ResponseEntity<ReservationResponse> annulerReservation(@PathVariable String reference) {
        return ResponseEntity.ok(reservationService.annulerReservation(reference));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Lister les réservations d'un client")
    public ResponseEntity<List<ReservationResponse>> getReservationsParClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(reservationService.getReservationsParClient(clientId));
    }

    @GetMapping("/event/{evenementId}")
    @Operation(summary = "Lister les réservations d'un événement")
    public ResponseEntity<List<ReservationResponse>> getReservationsParEvenement(@PathVariable Long evenementId) {
        return ResponseEntity.ok(reservationService.getReservationsParEvenement(evenementId));
    }
}
