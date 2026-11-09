package com.eventpro.repository;

import com.eventpro.entity.Reservation;
import com.eventpro.enums.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReference(String reference);

    List<Reservation> findByStatutAndDateExpirationBefore(StatutReservation statut, LocalDateTime dateExpiration);

    List<Reservation> findByClientId(Long clientId);

    List<Reservation> findByEvenementId(Long evenementId);

    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.lignes WHERE r.reference = :reference")
    Optional<Reservation> findByReferenceWithLignes(@Param("reference") String reference);
}
