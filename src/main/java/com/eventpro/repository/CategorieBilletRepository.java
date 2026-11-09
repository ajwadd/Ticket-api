package com.eventpro.repository;

import com.eventpro.entity.CategorieBillet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategorieBilletRepository extends JpaRepository<CategorieBillet, Long> {

    List<CategorieBillet> findByEvenementId(Long evenementId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CategorieBillet c WHERE c.id = :id")
    Optional<CategorieBillet> findByIdWithLock(@Param("id") Long id);
}
