package com.eventpro.service.impl;

import com.eventpro.dto.request.LigneReservationRequest;
import com.eventpro.dto.request.ReservationRequest;
import com.eventpro.dto.response.ReservationResponse;
import com.eventpro.entity.*;
import com.eventpro.enums.StatutEvenement;
import com.eventpro.enums.StatutReservation;
import com.eventpro.exception.InsufficientStockException;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.exception.ValidationException;
import com.eventpro.mapper.ReservationMapper;
import com.eventpro.repository.CategorieBilletRepository;
import com.eventpro.repository.ClientRepository;
import com.eventpro.repository.EvenementRepository;
import com.eventpro.repository.ReservationRepository;
import com.eventpro.service.PromotionService;
import com.eventpro.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final EvenementRepository evenementRepository;
    private final CategorieBilletRepository categorieBilletRepository;
    private final ClientRepository clientRepository;
    private final PromotionService promotionService;
    private final ReservationMapper reservationMapper;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservationResponse creerReservation(ReservationRequest request) {
        log.info("Tentative de réservation pour l'événement ID: {} par le client ID: {}",
                request.getEvenementId(), request.getClientId());

        Evenement evenement = evenementRepository.findById(request.getEvenementId())
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable avec l'ID: " + request.getEvenementId()));

        if (evenement.getStatut() != StatutEvenement.PUBLISHED) {
            throw new ValidationException("L'événement n'est pas ouvert à la vente (statut: " + evenement.getStatut() + ")");
        }

        LocalDateTime now = LocalDateTime.now();
        if (evenement.getDateOuvertureVente() != null && now.isBefore(evenement.getDateOuvertureVente())) {
            throw new ValidationException("Les ventes pour cet événement ne sont pas encore ouvertes.");
        }
        if (evenement.getDateClotureVente() != null && now.isAfter(evenement.getDateClotureVente())) {
            throw new ValidationException("Les ventes pour cet événement sont déjà closes.");
        }

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'ID: " + request.getClientId()));

        LocalDateTime dateCreation = LocalDateTime.now();
        LocalDateTime dateExpiration = dateCreation.plusMinutes(10);
        String reference = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Reservation reservation = Reservation.builder()
                .reference(reference)
                .evenement(evenement)
                .client(client)
                .dateCreation(dateCreation)
                .dateExpiration(dateExpiration)
                .statut(StatutReservation.PENDING_PAYMENT)
                .lignes(new ArrayList<>())
                .build();

        // Verrouillage pessimiste sur chaque catégorie et réservation atomique de stock
        for (LigneReservationRequest ligneReq : request.getLignes()) {
            CategorieBillet categorie = categorieBilletRepository.findByIdWithLock(ligneReq.getCategorieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie de billet introuvable avec l'ID: " + ligneReq.getCategorieId()));

            if (!categorie.getEvenement().getId().equals(evenement.getId())) {
                throw new ValidationException("La catégorie " + categorie.getNom() + " n'appartient pas à l'événement sélectionné.");
            }

            if (ligneReq.getQuantite() > categorie.getQuotaMaxParClient()) {
                throw new ValidationException(String.format("La quantité demandée (%d) dépasse le quota maximum par client (%d) pour la catégorie %s",
                        ligneReq.getQuantite(), categorie.getQuotaMaxParClient(), categorie.getNom()));
            }

            int placesDisponibles = categorie.getPlacesDisponibles();
            if (ligneReq.getQuantite() > placesDisponibles) {
                log.warn("Stock insuffisant pour catégorie {}: demandé {}, disponible {}",
                        categorie.getNom(), ligneReq.getQuantite(), placesDisponibles);
                throw new InsufficientStockException(
                        String.format("Places insuffisantes pour la catégorie '%s'. Demandées: %d, Disponibles: %d",
                                categorie.getNom(), ligneReq.getQuantite(), placesDisponibles));
            }

            // Bloquer le stock
            categorie.reserver(ligneReq.getQuantite());
            categorieBilletRepository.save(categorie);

            LigneReservation ligne = LigneReservation.builder()
                    .reservation(reservation)
                    .categorieBillet(categorie)
                    .quantite(ligneReq.getQuantite())
                    .prixUnitaire(categorie.getPrixUnitaire())
                    .build();
            ligne.calculerSousTotal();
            reservation.getLignes().add(ligne);
        }

        // Traitement du code promotionnel éventuel
        if (request.getCodePromo() != null && !request.getCodePromo().isBlank()) {
            reservation.calculerTotal();
            Promotion promo = promotionService.validerEtObtenirPromotion(
                    request.getCodePromo(), evenement, reservation.getMontantBrut());
            if (promo != null) {
                promo.reserverQuota();
                reservation.setPromotion(promo);
            }
        }

        // Calculer montants finaux
        reservation.calculerTotal();

        Reservation saved = reservationRepository.save(reservation);
        log.info("Réservation créée avec succès : Ref={}, Expiration={}", saved.getReference(), saved.getDateExpiration());
        return reservationMapper.toReservationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationByReference(String reference) {
        Reservation reservation = reservationRepository.findByReferenceWithLignes(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable avec la référence: " + reference));
        return reservationMapper.toReservationResponse(reservation);
    }

    @Override
    @Transactional
    public ReservationResponse annulerReservation(String reference) {
        Reservation reservation = reservationRepository.findByReferenceWithLignes(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable avec la référence: " + reference));

        if (reservation.getStatut() != StatutReservation.PENDING_PAYMENT) {
            throw new ValidationException("Seules les réservations en attente de paiement peuvent être annulées manuellement.");
        }

        // Libération des places réservées
        for (LigneReservation ligne : reservation.getLignes()) {
            CategorieBillet cat = categorieBilletRepository.findByIdWithLock(ligne.getCategorieBillet().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));
            cat.libererReservation(ligne.getQuantite());
            categorieBilletRepository.save(cat);
        }

        // Libération du quota promotionnel
        if (reservation.getPromotion() != null) {
            promotionService.libererQuota(reservation.getPromotion());
        }

        reservation.annuler();
        Reservation saved = reservationRepository.save(reservation);
        log.info("Réservation {} annulée et places libérées", reference);
        return reservationMapper.toReservationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsParClient(Long clientId) {
        return reservationMapper.toReservationResponseList(reservationRepository.findByClientId(clientId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsParEvenement(Long evenementId) {
        return reservationMapper.toReservationResponseList(reservationRepository.findByEvenementId(evenementId));
    }
}
