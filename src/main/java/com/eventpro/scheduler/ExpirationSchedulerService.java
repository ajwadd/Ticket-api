package com.eventpro.scheduler;

import com.eventpro.entity.CategorieBillet;
import com.eventpro.entity.LigneReservation;
import com.eventpro.entity.Reservation;
import com.eventpro.enums.StatutReservation;
import com.eventpro.repository.CategorieBilletRepository;
import com.eventpro.repository.ReservationRepository;
import com.eventpro.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpirationSchedulerService {

    private final ReservationRepository reservationRepository;
    private final CategorieBilletRepository categorieBilletRepository;
    private final PromotionService promotionService;

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void libererReservationsExpirees() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> reservationsExpirees = reservationRepository
                .findByStatutAndDateExpirationBefore(StatutReservation.PENDING_PAYMENT, now);

        if (reservationsExpirees.isEmpty()) {
            return;
        }

        log.info("Traitement de {} réservations expirées...", reservationsExpirees.size());

        for (Reservation reservation : reservationsExpirees) {
            try {
                // Remise en vente immédiate des places réservées
                for (LigneReservation ligne : reservation.getLignes()) {
                    CategorieBillet categorie = categorieBilletRepository.findByIdWithLock(ligne.getCategorieBillet().getId())
                            .orElse(null);
                    if (categorie != null) {
                        categorie.libererReservation(ligne.getQuantite());
                        categorieBilletRepository.save(categorie);
                        log.debug("Libération de {} places pour catégorie {}", ligne.getQuantite(), categorie.getNom());
                    }
                }

                // Libération du quota promotionnel si présent
                if (reservation.getPromotion() != null) {
                    promotionService.libererQuota(reservation.getPromotion());
                    log.debug("Libération du quota promo {}", reservation.getPromotion().getCode());
                }

                // Bascule du statut à EXPIRED
                reservation.expirer();
                reservationRepository.save(reservation);
                log.info("Réservation {} passée à EXPIRED avec succès.", reservation.getReference());

            } catch (Exception e) {
                log.error("Erreur lors de la libération de la réservation {}", reservation.getReference(), e);
            }
        }
    }
}
