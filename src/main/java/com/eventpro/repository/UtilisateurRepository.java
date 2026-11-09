package com.eventpro.repository;

import com.eventpro.entity.Client;
import com.eventpro.entity.Organisateur;
import com.eventpro.entity.Administrateur;
import com.eventpro.entity.AgentControle;
import com.eventpro.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
}
