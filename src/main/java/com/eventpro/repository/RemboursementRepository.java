package com.eventpro.repository;

import com.eventpro.entity.Remboursement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RemboursementRepository extends JpaRepository<Remboursement, Long> {

    List<Remboursement> findByPaiementId(Long paiementId);
}
