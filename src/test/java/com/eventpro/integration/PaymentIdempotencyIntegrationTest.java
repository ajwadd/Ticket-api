package com.eventpro.integration;

import com.eventpro.dto.request.PaymentRequest;
import com.eventpro.dto.response.PaymentResponse;
import com.eventpro.entity.*;
import com.eventpro.enums.StatutEvenement;
import com.eventpro.enums.StatutPaiement;
import com.eventpro.enums.StatutReservation;
import com.eventpro.exception.IdempotencyConflictException;
import com.eventpro.repository.*;
import com.eventpro.service.PaiementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentIdempotencyIntegrationTest {

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private CategorieBilletRepository categorieBilletRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private LigneReservationRepository ligneReservationRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        Client client = Client.builder()
                .nom("Client Test")
                .email("client_pay_" + System.currentTimeMillis() + "@test.com")
                .telephone("0611223344")
                .build();
        client = clientRepository.save(client);

        Evenement evenement = Evenement.builder()
                .titre("Concert Paiement")
                .lieu("Rabat")
                .dateDebut(LocalDateTime.now().plusDays(10))
                .dateFin(LocalDateTime.now().plusDays(10).plusHours(3))
                .statut(StatutEvenement.PUBLISHED)
                .build();
        evenement = evenementRepository.save(evenement);

        CategorieBillet cat = CategorieBillet.builder()
                .nom("Orchestre")
                .prixUnitaire(new BigDecimal("200.00"))
                .quantiteTotale(100)
                .placesReservees(2)
                .placesVendues(0)
                .evenement(evenement)
                .build();
        cat = categorieBilletRepository.save(cat);

        reservation = Reservation.builder()
                .reference("RES-PAY-" + System.currentTimeMillis())
                .evenement(evenement)
                .client(client)
                .dateCreation(LocalDateTime.now())
                .dateExpiration(LocalDateTime.now().plusMinutes(10))
                .statut(StatutReservation.PENDING_PAYMENT)
                .montantBrut(new BigDecimal("400.00"))
                .montantReduction(BigDecimal.ZERO)
                .montantTotal(new BigDecimal("400.00"))
                .lignes(new ArrayList<>())
                .build();
        reservation = reservationRepository.save(reservation);

        LigneReservation ligne = LigneReservation.builder()
                .reservation(reservation)
                .categorieBillet(cat)
                .quantite(2)
                .prixUnitaire(new BigDecimal("200.00"))
                .sousTotal(new BigDecimal("400.00"))
                .build();
        ligne = ligneReservationRepository.save(ligne);
        reservation.getLignes().add(ligne);
    }

    @Test
    @DisplayName("Paiement idempotent : même clé + même payload -> renvoie le résultat archivé (200 OK)")
    void testPaiementIdempotentReplay() {
        String idempotencyKey = "key-idem-" + System.currentTimeMillis();

        PaymentRequest request = PaymentRequest.builder()
                .reservationReference(reservation.getReference())
                .montant(new BigDecimal("400.00"))
                .build();

        // 1er appel : succès et création des billets
        PaymentResponse premierPaiement = paiementService.effectuerPaiement(idempotencyKey, request);
        assertNotNull(premierPaiement);
        assertEquals(StatutPaiement.SUCCESS, premierPaiement.getStatut());
        assertEquals(2, premierPaiement.getBillets().size());

        // 2ème appel : Rejeu idempotent immédiat
        PaymentResponse rejeuPaiement = paiementService.effectuerPaiement(idempotencyKey, request);
        assertNotNull(rejeuPaiement);
        assertEquals(premierPaiement.getReference(), rejeuPaiement.getReference());
        assertEquals(premierPaiement.getBillets().size(), rejeuPaiement.getBillets().size());
    }

    @Test
    @DisplayName("Paiement idempotent : même clé + payload altéré -> rejet 409 Conflict")
    void testPaiementPayloadAltereRejete() {
        String idempotencyKey = "key-alter-" + System.currentTimeMillis();

        PaymentRequest request1 = PaymentRequest.builder()
                .reservationReference(reservation.getReference())
                .montant(new BigDecimal("400.00"))
                .build();

        paiementService.effectuerPaiement(idempotencyKey, request1);

        // Appel avec la même clé mais montant différent
        PaymentRequest requestAlt = PaymentRequest.builder()
                .reservationReference(reservation.getReference())
                .montant(new BigDecimal("100.00")) // Altération !
                .build();

        assertThrows(IdempotencyConflictException.class, () ->
                paiementService.effectuerPaiement(idempotencyKey, requestAlt));
    }
}
