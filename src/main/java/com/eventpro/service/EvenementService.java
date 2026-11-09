package com.eventpro.service;

import com.eventpro.dto.request.CategoryUpdateRequest;
import com.eventpro.dto.request.EventCreateRequest;
import com.eventpro.dto.response.CategoryResponse;
import com.eventpro.dto.response.EventResponse;

import java.util.List;

public interface EvenementService {

    EventResponse creerEvenement(EventCreateRequest request);

    EventResponse publierEvenement(Long evenementId);

    EventResponse annulerEvenement(Long evenementId);

    CategoryResponse modifierCategorie(Long categorieId, CategoryUpdateRequest request);

    EventResponse getEvenementById(Long evenementId);

    List<EventResponse> getCataloguePublic();

    List<EventResponse> getEvenementsParOrganisateur(Long organisateurId);
}
