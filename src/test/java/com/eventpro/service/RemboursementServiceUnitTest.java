package com.eventpro.service;

import com.eventpro.dto.request.RefundRequest;
import com.eventpro.dto.response.RefundResponse;
import com.eventpro.entity.Evenement;
import com.eventpro.entity.Paiement;
import com.eventpro.entity.Reservation;
import com.eventpro.enums.StatutBillet;
import com.eventpro.enums.StatutEvenement;
import com.eventpro.enums.StatutPaiement;
import com.eventpro.enums.StatutRemboursement;
import com.eventpro.repository.BilletRepository;
import com.eventpro.repository.PaiementRepository;
import com.eventpro.repository.RemboursementRepository;
import com.eventpro.repository.ReservationRepository;
import com.eventpro.service.impl.RemboursementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemboursementServiceUnitTest {

    @Mock
    private PaiementRepository paiementRepository;
    @Mock
    private RemboursementRepository remboursementRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private BilletRepository billetRepository;

    @InjectMocks
    private RemboursementServiceImpl remboursementService;

    private Paiement paiement;
    private Reservation reservation;
    private Evenement evenement;

    @BeforeEach
    void setUp() {
        evenement = Evenement.builder()
                .id(1L)
                .titre("Concert Casablanca")
                .statut(StatutEvenement.PUBLISHED)
                .build();

        reservation = Reservation.builder()
                .id(100L)
                .reference("RES-TEST-01")
                .evenement(evenement)
                .montantTotal(new BigDecimal("640.00")) // Montant net payé
                .build();

        paiement = Paiement.builder()
                .id(200L)
                .reference("PAY-TEST-01")
                .reservation(reservation)
                .montant(new BigDecimal("640.00"))
                .statut(StatutPaiement.SUCCESS)
                .build();

        when(remboursementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Remboursement > 7 jours avant : 100% remboursé, 0% frais, billets invalidés")
    void testRemboursementPlusDe7Jours() {
        evenement.setDateDebut(LocalDateTime.now().plusDays(10));
        when(paiementRepository.findByReference("PAY-TEST-01")).thenReturn(Optional.of(paiement));

        RefundRequest request = RefundRequest.builder()
                .paiementReference("PAY-TEST-01")
                .motif("Empêchement")
                .build();

        RefundResponse response = remboursementService.traiterDemandeRemboursement(request);

        assertEquals(100, response.getPourcentageRembourse());
        assertEquals(0, response.getPourcentageFraisRetenus());
        assertEquals(new BigDecimal("640.00"), response.getMontantRembourse());
        assertEquals(StatutRemboursement.APPROVED, response.getStatut());

        verify(billetRepository).updateStatutForReservation(100L, StatutBillet.CANCELLED);
    }

    @Test
    @DisplayName("Remboursement entre 3 et 7 jours : 50% remboursé, 50% frais, billets invalidés")
    void testRemboursementEntre3Et7Jours() {
        evenement.setDateDebut(LocalDateTime.now().plusDays(5));
        when(paiementRepository.findByReference("PAY-TEST-01")).thenReturn(Optional.of(paiement));

        RefundRequest request = RefundRequest.builder()
                .paiementReference("PAY-TEST-01")
                .motif("Changement de plan")
                .build();

        RefundResponse response = remboursementService.traiterDemandeRemboursement(request);

        assertEquals(50, response.getPourcentageRembourse());
        assertEquals(50, response.getPourcentageFraisRetenus());
        assertEquals(new BigDecimal("320.00"), response.getMontantRembourse());
        assertEquals(StatutRemboursement.APPROVED, response.getStatut());

        verify(billetRepository).updateStatutForReservation(100L, StatutBillet.CANCELLED);
    }

    @Test
    @DisplayName("Remboursement moins de 3 jours : 0% remboursé (refusé), billets maintenus")
    void testRemboursementMoinsDe3Jours() {
        evenement.setDateDebut(LocalDateTime.now().plusDays(2));
        when(paiementRepository.findByReference("PAY-TEST-01")).thenReturn(Optional.of(paiement));

        RefundRequest request = RefundRequest.builder()
                .paiementReference("PAY-TEST-01")
                .motif("Demande tardive")
                .build();

        RefundResponse response = remboursementService.traiterDemandeRemboursement(request);

        assertEquals(0, response.getPourcentageRembourse());
        assertEquals(100, response.getPourcentageFraisRetenus());
        assertEquals(new BigDecimal("0.00"), response.getMontantRembourse());
        assertEquals(StatutRemboursement.REJECTED, response.getStatut());

        verify(billetRepository, never()).updateStatutForReservation(any(), any());
    }

    @Test
    @DisplayName("Événement annulé par l'organisateur : 100% remboursé même à la veille")
    void testRemboursementEvenementAnnule() {
        evenement.setStatut(StatutEvenement.CANCELLED);
        evenement.setDateDebut(LocalDateTime.now().plusHours(12)); // Moins de 24h
        when(paiementRepository.findByReference("PAY-TEST-01")).thenReturn(Optional.of(paiement));

        RefundRequest request = RefundRequest.builder()
                .paiementReference("PAY-TEST-01")
                .motif("Annulation concert")
                .build();

        RefundResponse response = remboursementService.traiterDemandeRemboursement(request);

        assertEquals(100, response.getPourcentageRembourse());
        assertEquals(new BigDecimal("640.00"), response.getMontantRembourse());
        assertEquals(StatutRemboursement.APPROVED, response.getStatut());
    }
}
