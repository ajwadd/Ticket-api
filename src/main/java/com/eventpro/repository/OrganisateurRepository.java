package com.eventpro.repository;

import com.eventpro.entity.Organisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganisateurRepository extends JpaRepository<Organisateur, Long> {
    Optional<Organisateur> findByEmail(String email);
}
