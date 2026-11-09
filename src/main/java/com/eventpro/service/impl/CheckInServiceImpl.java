package com.eventpro.service.impl;

import com.eventpro.dto.request.CheckInRequest;
import com.eventpro.dto.response.CheckInResponse;
import com.eventpro.entity.Billet;
import com.eventpro.enums.StatutBillet;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.exception.TicketAlreadyUsedException;
import com.eventpro.repository.BilletRepository;
import com.eventpro.service.CheckInService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    private final BilletRepository billetRepository;

    @Override
    @Transactional
    public CheckInResponse composterBillet(CheckInRequest request) {
        log.info("Tentative de compostage du billet {} pour événement {}", request.getCodeUnique(), request.getEvenementId());
        LocalDateTime now = LocalDateTime.now();

        // Exécution de l'update atomique direct en SQL pour éviter la race condition de deux scans simultanés
        int lignesModifiees = billetRepository.checkInTicketAtomic(
                request.getCodeUnique(),
                request.getEvenementId(),
                now
        );

        if (lignesModifiees == 1) {
            log.info("Compostage réussi pour le billet {}", request.getCodeUnique());
            Billet billet = billetRepository.findByCode(request.getCodeUnique()).orElseThrow();
            return CheckInResponse.builder()
                    .success(true)
                    .message("Billet validé avec succès. Entrée acceptée.")
                    .codeUnique(billet.getCode())
                    .statut(StatutBillet.USED)
                    .dateControle(now)
                    .nomPorteur(billet.getNomPorteur())
                    .categorieNom(billet.getCategorieBillet().getNom())
                    .build();
        }

        // Si lignesModifiees == 0, analyser la raison exacte pour un code HTTP précis (404 vs 409)
        Billet billet = billetRepository.findByCode(request.getCodeUnique())
                .orElseThrow(() -> new ResourceNotFoundException("Billet introuvable avec le code: " + request.getCodeUnique()));

        if (!billet.getEvenement().getId().equals(request.getEvenementId())) {
            throw new TicketAlreadyUsedException("Le billet présenté n'appartient pas à cet événement.");
        }

        if (billet.getStatut() == StatutBillet.USED) {
            throw new TicketAlreadyUsedException("Le billet présenté a déjà été scanné à l'entrée à " + billet.getDateControle());
        }

        if (billet.getStatut() == StatutBillet.CANCELLED) {
            throw new TicketAlreadyUsedException("Le billet présenté a été annulé.");
        }

        throw new TicketAlreadyUsedException("Le billet ne peut pas être composté (statut invalide: " + billet.getStatut() + ")");
    }
}
