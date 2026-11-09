package com.eventpro.integration;

import com.eventpro.dto.request.LigneReservationRequest;
import com.eventpro.dto.request.ReservationRequest;
import com.eventpro.dto.response.ReservationResponse;
import com.eventpro.entity.CategorieBillet;
import com.eventpro.entity.Client;
import com.eventpro.entity.Evenement;
import com.eventpro.enums.StatutEvenement;
import com.eventpro.exception.InsufficientStockException;
import com.eventpro.repository.CategorieBilletRepository;
import com.eventpro.repository.ClientRepository;
import com.eventpro.repository.EvenementRepository;
import com.eventpro.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class ReservationConcurrencyIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private CategorieBilletRepository categorieBilletRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Evenement evenement;
    private CategorieBillet categorie;
    private Client client1;
    private Client client2;

    @BeforeEach
    void setUp() {
        evenement = Evenement.builder()
                .titre("Festival Concurrence")
                .lieu("Rabat")
                .dateDebut(LocalDateTime.now().plusDays(20))
                .dateFin(LocalDateTime.now().plusDays(20).plusHours(5))
                .statut(StatutEvenement.PUBLISHED)
                .build();
        evenement = evenementRepository.save(evenement);

        // Capacité critique : seulement 1 place disponible !
        categorie = CategorieBillet.builder()
                .nom("LastTicketVIP")
                .prixUnitaire(new BigDecimal("500.00"))
                .quantiteTotale(1)
                .placesReservees(0)
                .placesVendues(0)
                .quotaMaxParClient(5)
                .evenement(evenement)
                .build();
        categorie = categorieBilletRepository.save(categorie);

        client1 = Client.builder()
                .nom("Acheteur 1")
                .email("buyer1_" + System.currentTimeMillis() + "@test.com")
                .telephone("0600000001")
                .build();
        client1 = clientRepository.save(client1);

        client2 = Client.builder()
                .nom("Acheteur 2")
                .email("buyer2_" + System.currentTimeMillis() + "@test.com")
                .telephone("0600000002")
                .build();
        client2 = clientRepository.save(client2);
    }

    @Test
    @DisplayName("Concurrence pessimiste : 2 requêtes simultanées pour 1 seule place restante -> 1 succès et 1 rejet 409")
    void testReservationConcurrenteSurDernierePlace() throws InterruptedException {
        int nbThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(nbThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(nbThreads);

        AtomicInteger succesCount = new AtomicInteger(0);
        AtomicInteger conflitCount = new AtomicInteger(0);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        Runnable task1 = () -> {
            try {
                startLatch.await();
                ReservationRequest req = ReservationRequest.builder()
                        .evenementId(evenement.getId())
                        .clientId(client1.getId())
                        .lignes(List.of(LigneReservationRequest.builder()
                                .categorieId(categorie.getId())
                                .quantite(1)
                                .build()))
                        .build();
                ReservationResponse resp = reservationService.creerReservation(req);
                if (resp != null) {
                    succesCount.incrementAndGet();
                }
            } catch (InsufficientStockException e) {
                conflitCount.incrementAndGet();
            } catch (Throwable t) {
                exceptions.add(t);
            } finally {
                endLatch.countDown();
            }
        };

        Runnable task2 = () -> {
            try {
                startLatch.await();
                ReservationRequest req = ReservationRequest.builder()
                        .evenementId(evenement.getId())
                        .clientId(client2.getId())
                        .lignes(List.of(LigneReservationRequest.builder()
                                .categorieId(categorie.getId())
                                .quantite(1)
                                .build()))
                        .build();
                ReservationResponse resp = reservationService.creerReservation(req);
                if (resp != null) {
                    succesCount.incrementAndGet();
                }
            } catch (InsufficientStockException e) {
                conflitCount.incrementAndGet();
            } catch (Throwable t) {
                exceptions.add(t);
            } finally {
                endLatch.countDown();
            }
        };

        executorService.submit(task1);
        executorService.submit(task2);

        // Déclencher le départ simultané exact des 2 threads
        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertEquals(1, succesCount.get(), "Exactement 1 réservation doit réussir");
        assertEquals(1, conflitCount.get(), "L'autre tentative concurrente doit être rejetée avec InsufficientStockException");

        // Vérifier l'état final de la catégorie en base de données
        CategorieBillet catApres = categorieBilletRepository.findById(categorie.getId()).orElseThrow();
        assertEquals(1, catApres.getPlacesReservees(), "Le nombre de places réservées doit être de 1");
        assertEquals(0, catApres.getPlacesDisponibles(), "Il doit rester 0 place disponible");
    }
}
