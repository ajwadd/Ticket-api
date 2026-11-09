package com.eventpro.repository;

import com.eventpro.entity.AgentControle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentControleRepository extends JpaRepository<AgentControle, Long> {
    Optional<AgentControle> findByEmail(String email);
}
