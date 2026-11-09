package com.eventpro.repository;

import com.eventpro.entity.Evenement;
import com.eventpro.enums.StatutEvenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, Long> {

    List<Evenement> findByStatut(StatutEvenement statut);

    List<Evenement> findByOrganisateurId(Long organisateurId);
}
