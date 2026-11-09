package com.eventpro.repository;

import com.eventpro.entity.Billet;
import com.eventpro.enums.StatutBillet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BilletRepository extends JpaRepository<Billet, Long> {

    Optional<Billet> findByCode(String code);

    List<Billet> findByEvenementId(Long evenementId);

    @Query("SELECT b FROM Billet b WHERE b.ligneReservation.reservation.id = :reservationId")
    List<Billet> findByReservationId(@Param("reservationId") Long reservationId);

    @Modifying
    @Query("UPDATE Billet b SET b.statut = 'USED', b.dateControle = :dateControle " +
           "WHERE b.code = :code AND b.statut = 'VALID' AND b.evenement.id = :evenementId")
    int checkInTicketAtomic(@Param("code") String code,
                            @Param("evenementId") Long evenementId,
                            @Param("dateControle") LocalDateTime dateControle);

    @Modifying
    @Query("UPDATE Billet b SET b.statut = :nouveauStatut " +
           "WHERE b.ligneReservation.reservation.id = :reservationId")
    int updateStatutForReservation(@Param("reservationId") Long reservationId,
                                   @Param("nouveauStatut") StatutBillet nouveauStatut);

    @Modifying
    @Query("UPDATE Billet b SET b.statut = :nouveauStatut WHERE b.evenement.id = :evenementId")
    int updateStatutForEvenement(@Param("evenementId") Long evenementId,
                                 @Param("nouveauStatut") StatutBillet nouveauStatut);
}
