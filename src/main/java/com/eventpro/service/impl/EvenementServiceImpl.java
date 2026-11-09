package com.eventpro.service.impl;

import com.eventpro.dto.request.CategoryUpdateRequest;
import com.eventpro.dto.request.EventCreateRequest;
import com.eventpro.dto.response.CategoryResponse;
import com.eventpro.dto.response.EventResponse;
import com.eventpro.entity.CategorieBillet;
import com.eventpro.entity.Evenement;
import com.eventpro.entity.Organisateur;
import com.eventpro.enums.StatutBillet;
import com.eventpro.enums.StatutEvenement;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.exception.ValidationException;
import com.eventpro.mapper.EventMapper;
import com.eventpro.repository.BilletRepository;
import com.eventpro.repository.CategorieBilletRepository;
import com.eventpro.repository.EvenementRepository;
import com.eventpro.repository.OrganisateurRepository;
import com.eventpro.service.EvenementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EvenementServiceImpl implements EvenementService {

    private final EvenementRepository evenementRepository;
    private final CategorieBilletRepository categorieBilletRepository;
    private final OrganisateurRepository organisateurRepository;
    private final BilletRepository billetRepository;
    private final EventMapper eventMapper;

    @Override
    public EventResponse creerEvenement(EventCreateRequest request) {
        log.info("Création d'un nouvel événement : {}", request.getTitre());

        if (request.getDateDebut().isAfter(request.getDateFin())) {
            throw new ValidationException("La date de début doit être antérieure à la date de fin");
        }

        Organisateur organisateur = organisateurRepository.findById(request.getOrganisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("Organisateur introuvable avec l'ID: " + request.getOrganisateurId()));

        Evenement evenement = eventMapper.toEvenement(request);
        evenement.setStatut(StatutEvenement.DRAFT);
        evenement.setOrganisateur(organisateur);
        evenement.setCategories(new ArrayList<>());

        if (request.getCategories() != null) {
            for (var catReq : request.getCategories()) {
                CategorieBillet cat = eventMapper.toCategorieBillet(catReq);
                cat.setEvenement(evenement);
                cat.setPlacesReservees(0);
                cat.setPlacesVendues(0);
                evenement.getCategories().add(cat);
            }
        }

        Evenement saved = evenementRepository.save(evenement);
        return eventMapper.toEventResponse(saved);
    }

    @Override
    public EventResponse publierEvenement(Long evenementId) {
        log.info("Publication de l'événement ID: {}", evenementId);
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable avec l'ID: " + evenementId));

        evenement.publier();
        return eventMapper.toEventResponse(evenement);
    }

    @Override
    public EventResponse annulerEvenement(Long evenementId) {
        log.info("Annulation de l'événement ID: {}", evenementId);
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable avec l'ID: " + evenementId));

        evenement.annuler();
        // Invalidation atomique des billets de l'événement
        billetRepository.updateStatutForEvenement(evenementId, StatutBillet.CANCELLED);

        return eventMapper.toEventResponse(evenement);
    }

    @Override
    public CategoryResponse modifierCategorie(Long categorieId, CategoryUpdateRequest request) {
        log.info("Modification de la catégorie ID: {}", categorieId);
        CategorieBillet categorie = categorieBilletRepository.findByIdWithLock(categorieId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable avec l'ID: " + categorieId));

        int placesEngagees = categorie.getPlacesReservees() + categorie.getPlacesVendues();

        if (request.getQuantiteTotale() != null) {
            if (request.getQuantiteTotale() < placesEngagees) {
                throw new ValidationException(
                        String.format("Impossible de réduire la capacité à %d : %d places sont déjà engagées (%d réservées, %d vendues)",
                                request.getQuantiteTotale(), placesEngagees, categorie.getPlacesReservees(), categorie.getPlacesVendues()));
            }
            categorie.setQuantiteTotale(request.getQuantiteTotale());
        }

        if (request.getNom() != null && !request.getNom().isBlank()) {
            categorie.setNom(request.getNom());
        }
        if (request.getPrixUnitaire() != null) {
            categorie.setPrixUnitaire(request.getPrixUnitaire());
        }
        if (request.getQuotaMaxParClient() != null) {
            categorie.setQuotaMaxParClient(request.getQuotaMaxParClient());
        }

        CategorieBillet updated = categorieBilletRepository.save(categorie);
        return eventMapper.toCategoryResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEvenementById(Long evenementId) {
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable avec l'ID: " + evenementId));
        return eventMapper.toEventResponse(evenement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getCataloguePublic() {
        List<Evenement> evenements = evenementRepository.findByStatut(StatutEvenement.PUBLISHED);
        return eventMapper.toEventResponseList(evenements);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEvenementsParOrganisateur(Long organisateurId) {
        List<Evenement> evenements = evenementRepository.findByOrganisateurId(organisateurId);
        return eventMapper.toEventResponseList(evenements);
    }
}
