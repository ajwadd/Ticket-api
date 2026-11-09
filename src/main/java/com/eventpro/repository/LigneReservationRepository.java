package com.eventpro.repository;

import com.eventpro.entity.LigneReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LigneReservationRepository extends JpaRepository<LigneReservation, Long> {

    List<LigneReservation> findByReservationId(Long reservationId);
}
