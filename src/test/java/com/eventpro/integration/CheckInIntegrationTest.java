package com.eventpro.integration;

import com.eventpro.dto.request.CheckInRequest;
import com.eventpro.dto.response.CheckInResponse;
import com.eventpro.entity.*;
import com.eventpro.enums.StatutBillet;
import com.eventpro.enums.StatutEvenement;
import com.eventpro.enums.StatutReservation;
import com.eventpro.exception.TicketAlreadyUsedException;
import com.eventpro.repository.*;
import com.eventpro.service.CheckInService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class CheckInIntegrationTest {

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private BilletRepository billetRepository;

    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private CategorieBilletRepository categorieBilletRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private LigneReservationRepository ligneReservationRepository;

    private Evenement evenement;
    private Billet billet;

    @BeforeEach
    void setUp() {
        evenement = Evenement.builder()
                .titre("Concert Check-in Test")
                .lieu("Casablanca")
                .dateDebut(LocalDateTime.now().plusDays(2))
                .dateFin(LocalDateTime.now().plusDays(2).plusHours(3))
                .statut(StatutEvenement.PUBLISHED)
                .build();
        evenement = evenementRepository.save(evenement);

        CategorieBillet cat = CategorieBillet.builder()
                .nom("VIP")
                .prixUnitaire(new BigDecimal("400.00"))
                .quantiteTotale(50)
                .evenement(evenement)
                .build();
        cat = categorieBilletRepository.save(cat);

        Reservation res = Reservation.builder()
                .reference("RES-CHK-" + System.currentTimeMillis())
                .evenement(evenement)
                .dateCreation(LocalDateTime.now())
                .dateExpiration(LocalDateTime.now().plusMinutes(10))
                .statut(StatutReservation.CONFIRMED)
                .build();
        res = reservationRepository.save(res);

        LigneReservation ligne = LigneReservation.builder()
                .reservation(res)
                .categorieBillet(cat)
                .quantite(1)
                .prixUnitaire(cat.getPrixUnitaire())
                .sousTotal(cat.getPrixUnitaire())
                .build();
        ligne = ligneReservationRepository.save(ligne);

        billet = Billet.builder()
                .code("tk_test_" + System.currentTimeMillis())
                .dateEmission(LocalDateTime.now())
                .statut(StatutBillet.VALID)
                .evenement(evenement)
                .categorieBillet(cat)
                .ligneReservation(ligne)
                .nomPorteur("Karim Idrissi")
                .build();
        billet = billetRepository.save(billet);
    }

    @Test
    @DisplayName("Check-in atomique : 1er scan valide (200 OK / USED), 2ème scan rejeté (TicketAlreadyUsedException / 409)")
    void testCheckInUnicite() {
        CheckInRequest request = CheckInRequest.builder()
                .codeUnique(billet.getCode())
                .evenementId(evenement.getId())
                .build();

        // 1er compostage
        CheckInResponse premierScan = checkInService.composterBillet(request);
        assertNotNull(premierScan);
        assertTrue(premierScan.isSuccess());
        assertEquals(StatutBillet.USED, premierScan.getStatut());

        // 2ème compostage avec le même billet
        assertThrows(TicketAlreadyUsedException.class, () -> checkInService.composterBillet(request));
    }
}
