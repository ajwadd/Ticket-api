package com.eventpro.service.impl;

import com.eventpro.dto.request.PaymentRequest;
import com.eventpro.dto.response.PaymentResponse;
import com.eventpro.dto.response.TicketResponse;
import com.eventpro.entity.*;
import com.eventpro.enums.StatutPaiement;
import com.eventpro.enums.StatutReservation;
import com.eventpro.exception.IdempotencyConflictException;
import com.eventpro.exception.ReservationExpiredException;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.exception.ValidationException;
import com.eventpro.repository.CategorieBilletRepository;
import com.eventpro.repository.PaiementRepository;
import com.eventpro.repository.ReservationRepository;
import com.eventpro.service.BilletService;
import com.eventpro.service.PaiementService;
import com.eventpro.service.PromotionService;
import com.eventpro.util.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements PaiementService {

    private final PaiementRepository paiementRepository;
    private final ReservationRepository reservationRepository;
    private final CategorieBilletRepository categorieBilletRepository;
    private final BilletService billetService;
    private final PromotionService promotionService;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PaymentResponse effectuerPaiement(String idempotencyKey, PaymentRequest request) {
        log.info("Traitement paiement : Clé Idempotence={}, Réservation={}", idempotencyKey, request.getReservationReference());

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ValidationException("L'en-tête Idempotency-Key est obligatoire pour effectuer un paiement.");
        }

        String payloadSignature = request.getReservationReference().trim() + "|" + request.getMontant().toPlainString();
        String currentHash = HashUtils.sha256(payloadSignature);

        // Contrôle d'Idempotence strict
        Optional<Paiement> paiementExistantOpt = paiementRepository.findByIdempotencyKey(idempotencyKey);
        if (paiementExistantOpt.isPresent()) {
            Paiement paiementExistant = paiementExistantOpt.get();
            if (paiementExistant.getRequestHash().equals(currentHash)) {
                log.info("Idempotent replay détecté pour la clé {}", idempotencyKey);
                List<TicketResponse> billets = billetService.getBilletsParReservation(paiementExistant.getReservation().getId());
                return toPaymentResponse(paiementExistant, billets);
            } else {
                log.warn("Même clé idempotence mais charge utile altérée !");
                throw new IdempotencyConflictException("Cette clé d'idempotence a déjà été utilisée avec des paramètres différents.");
            }
        }

        // Récupération de la réservation
        Reservation reservation = reservationRepository.findByReferenceWithLignes(request.getReservationReference())
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable avec la référence: " + request.getReservationReference()));

        // Interdire une nouvelle clé pour une réservation déjà payée
        if (reservation.getStatut() == StatutReservation.CONFIRMED ||
                paiementRepository.findByReservationReference(reservation.getReference()).isPresent()) {
            throw new IdempotencyConflictException("Cette réservation a déjà été payée avec succès.");
        }

        // Double contrôle d'expiration
        LocalDateTime now = LocalDateTime.now();
        if (reservation.getStatut() != StatutReservation.PENDING_PAYMENT || now.isAfter(reservation.getDateExpiration())) {
            log.warn("Tentative de paiement d'une réservation expirée ou invalide. Statut={}, Expiration={}",
                    reservation.getStatut(), reservation.getDateExpiration());
            throw new ReservationExpiredException("Le paiement arrive trop tard : les 10 minutes sont déjà écoulées ou la réservation est invalide.");
        }

        // Vérification de la concordance du montant
        if (request.getMontant().compareTo(reservation.getMontantTotal()) != 0) {
            throw new ValidationException(String.format("Montant incorrect : attendu %s MAD, reçu %s MAD",
                    reservation.getMontantTotal().toPlainString(), request.getMontant().toPlainString()));
        }

        // Transfert de stock : placesReservees -> placesVendues sous verrou
        for (LigneReservation ligne : reservation.getLignes()) {
            CategorieBillet cat = categorieBilletRepository.findByIdWithLock(ligne.getCategorieBillet().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));
            cat.confirmerVente(ligne.getQuantite());
            categorieBilletRepository.save(cat);
        }

        // Consommation définitive du quota promo
        if (reservation.getPromotion() != null) {
            promotionService.consommerQuota(reservation.getPromotion());
        }

        // Confirmer la réservation
        reservation.confirmer();
        reservationRepository.save(reservation);

        // Générer les billets avec QR Codes Base64
        List<TicketResponse> billets = billetService.genererBilletsPourReservation(reservation);

        // Enregistrer le paiement
        String referencePaiement = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Paiement paiement = Paiement.builder()
                .reference(referencePaiement)
                .reservation(reservation)
                .montant(reservation.getMontantTotal())
                .datePaiement(now)
                .statut(StatutPaiement.SUCCESS)
                .idempotencyKey(idempotencyKey)
                .requestHash(currentHash)
                .build();

        Paiement savedPaiement = paiementRepository.save(paiement);
        log.info("Paiement validé avec succès. Référence={}, Montant={} MAD", savedPaiement.getReference(), savedPaiement.getMontant());

        return toPaymentResponse(savedPaiement, billets);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaiementByReference(String reference) {
        Paiement paiement = paiementRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable avec la référence: " + reference));
        List<TicketResponse> billets = billetService.getBilletsParReservation(paiement.getReservation().getId());
        return toPaymentResponse(paiement, billets);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaiementByReservation(String reservationReference) {
        Paiement paiement = paiementRepository.findByReservationReference(reservationReference)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun paiement trouvé pour la réservation: " + reservationReference));
        List<TicketResponse> billets = billetService.getBilletsParReservation(paiement.getReservation().getId());
        return toPaymentResponse(paiement, billets);
    }

    private PaymentResponse toPaymentResponse(Paiement paiement, List<TicketResponse> billets) {
        return PaymentResponse.builder()
                .id(paiement.getId())
                .reference(paiement.getReference())
                .reservationReference(paiement.getReservation().getReference())
                .montant(paiement.getMontant())
                .datePaiement(paiement.getDatePaiement())
                .statut(paiement.getStatut())
                .billets(billets)
                .build();
    }
}
