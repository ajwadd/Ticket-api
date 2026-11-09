package com.eventpro.service.impl;

import com.eventpro.dto.request.RefundRequest;
import com.eventpro.dto.response.RefundResponse;
import com.eventpro.entity.Evenement;
import com.eventpro.entity.Paiement;
import com.eventpro.entity.Remboursement;
import com.eventpro.entity.Reservation;
import com.eventpro.enums.StatutBillet;
import com.eventpro.enums.StatutEvenement;
import com.eventpro.enums.StatutPaiement;
import com.eventpro.enums.StatutRemboursement;
import com.eventpro.enums.StatutReservation;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.exception.ValidationException;
import com.eventpro.repository.BilletRepository;
import com.eventpro.repository.PaiementRepository;
import com.eventpro.repository.RemboursementRepository;
import com.eventpro.repository.ReservationRepository;
import com.eventpro.service.RemboursementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RemboursementServiceImpl implements RemboursementService {

    private final PaiementRepository paiementRepository;
    private final RemboursementRepository remboursementRepository;
    private final ReservationRepository reservationRepository;
    private final BilletRepository billetRepository;

    @Override
    public RefundResponse traiterDemandeRemboursement(RefundRequest request) {
        log.info("Traitement de la demande de remboursement pour le paiement {}", request.getPaiementReference());

        Paiement paiement = paiementRepository.findByReference(request.getPaiementReference())
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable avec la référence: " + request.getPaiementReference()));

        if (paiement.getStatut() != StatutPaiement.SUCCESS) {
            throw new ValidationException("Impossible de rembourser un paiement non validé.");
        }

        Reservation reservation = paiement.getReservation();
        Evenement evenement = reservation.getEvenement();
        LocalDateTime now = LocalDateTime.now();

        BigDecimal montantNetPaye = paiement.getMontant(); // Montant net payé
        int tauxRemboursement;
        int tauxFrais;
        String message;

        // Événement annulé par l'organisateur -> 100% remboursé sans condition de délai
        if (evenement.getStatut() == StatutEvenement.CANCELLED) {
            tauxRemboursement = 100;
            tauxFrais = 0;
            message = "Événement annulé : Remboursement intégral accordé (100%).";
        } else {
            // Calcul du délai avant le début de l'événement
            Duration duration = Duration.between(now, evenement.getDateDebut());
            long joursAvantEvenement = duration.toDays();

            if (duration.isNegative()) {
                // Événement déjà commencé ou passé
                tauxRemboursement = 0;
                tauxFrais = 100;
                message = "Événement déjà passé : Demande de remboursement refusée (0%).";
            } else if (joursAvantEvenement > 7) {
                // > 7 jours avant -> 100% remboursé, 0% retenu
                tauxRemboursement = 100;
                tauxFrais = 0;
                message = "Demande reçue plus de 7 jours avant : Remboursement à 100%.";
            } else if (joursAvantEvenement >= 3) {
                // Entre 3 et 7 jours inclus -> 50% remboursé, 50% retenu
                tauxRemboursement = 50;
                tauxFrais = 50;
                message = "Demande reçue entre 3 et 7 jours avant : Remboursement partiel à 50%.";
            } else {
                // Moins de 3 jours avant -> 0% remboursé (Demande refusée)
                tauxRemboursement = 0;
                tauxFrais = 100;
                message = "Demande reçue moins de 3 jours avant : Demande refusée (0%). Les billets restent valides.";
            }
        }

        BigDecimal montantRembourse = montantNetPaye
                .multiply(BigDecimal.valueOf(tauxRemboursement))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        StatutRemboursement statutRemboursement = (tauxRemboursement > 0)
                ? StatutRemboursement.APPROVED
                : StatutRemboursement.REJECTED;

        // Mise à jour des billets si le remboursement est accordé
        if (tauxRemboursement > 0) {
            billetRepository.updateStatutForReservation(reservation.getId(), StatutBillet.CANCELLED);
            reservation.setStatut(StatutReservation.REFUNDED);
            reservationRepository.save(reservation);
        }

        Remboursement remboursement = Remboursement.builder()
                .paiement(paiement)
                .montant(montantRembourse)
                .motif(request.getMotif())
                .dateDemande(now)
                .statut(statutRemboursement)
                .build();

        Remboursement saved = remboursementRepository.save(remboursement);

        return RefundResponse.builder()
                .id(saved.getId())
                .paiementReference(paiement.getReference())
                .montantPaye(montantNetPaye)
                .montantRembourse(montantRembourse)
                .pourcentageRembourse(tauxRemboursement)
                .pourcentageFraisRetenus(tauxFrais)
                .motif(request.getMotif())
                .statut(statutRemboursement)
                .dateDemande(now)
                .message(message)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponse> getRemboursementsParPaiement(Long paiementId) {
        List<Remboursement> remboursements = remboursementRepository.findByPaiementId(paiementId);
        List<RefundResponse> responses = new ArrayList<>();
        for (Remboursement r : remboursements) {
            responses.add(RefundResponse.builder()
                    .id(r.getId())
                    .paiementReference(r.getPaiement().getReference())
                    .montantPaye(r.getPaiement().getMontant())
                    .montantRembourse(r.getMontant())
                    .motif(r.getMotif())
                    .statut(r.getStatut())
                    .dateDemande(r.getDateDemande())
                    .build());
        }
        return responses;
    }
}
