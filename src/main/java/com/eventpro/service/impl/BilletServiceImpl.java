package com.eventpro.service.impl;

import com.eventpro.dto.response.TicketResponse;
import com.eventpro.entity.Billet;
import com.eventpro.entity.LigneReservation;
import com.eventpro.entity.Reservation;
import com.eventpro.enums.StatutBillet;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.mapper.BilletMapper;
import com.eventpro.repository.BilletRepository;
import com.eventpro.service.BilletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BilletServiceImpl implements BilletService {

    private final BilletRepository billetRepository;
    private final BilletMapper billetMapper;

    @Override
    public List<TicketResponse> genererBilletsPourReservation(Reservation reservation) {
        log.info("Génération des billets pour la réservation {}", reservation.getReference());
        List<Billet> billetsGeneres = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        String nomPorteurParDefaut = reservation.getClient() != null ? reservation.getClient().getNom() : "Client EventPro";

        for (LigneReservation ligne : reservation.getLignes()) {
            for (int i = 0; i < ligne.getQuantite(); i++) {
                String codeUnique = "tk_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                Billet billet = Billet.builder()
                        .code(codeUnique)
                        .dateEmission(now)
                        .statut(StatutBillet.VALID)
                        .ligneReservation(ligne)
                        .categorieBillet(ligne.getCategorieBillet())
                        .evenement(reservation.getEvenement())
                        .nomPorteur(nomPorteurParDefaut)
                        .build();

                billetsGeneres.add(billetRepository.save(billet));
            }
        }

        log.info("{} billets générés avec succès pour la réservation {}", billetsGeneres.size(), reservation.getReference());
        return billetMapper.toTicketResponseList(billetsGeneres);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getBilletByCode(String code) {
        Billet billet = billetRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Billet introuvable avec le code: " + code));
        return billetMapper.toTicketResponse(billet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getBilletsParReservation(Long reservationId) {
        List<Billet> billets = billetRepository.findByReservationId(reservationId);
        return billetMapper.toTicketResponseList(billets);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getBilletsParEvenement(Long evenementId) {
        List<Billet> billets = billetRepository.findByEvenementId(evenementId);
        return billetMapper.toTicketResponseList(billets);
    }
}
