package com.eventpro.repository;

import com.eventpro.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    Optional<Paiement> findByReference(String reference);

    Optional<Paiement> findByIdempotencyKey(String idempotencyKey);

    Optional<Paiement> findByReservationReference(String reservationReference);

    Optional<Paiement> findByReservationId(Long reservationId);
}
