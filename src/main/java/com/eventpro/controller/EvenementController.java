package com.eventpro.controller;

import com.eventpro.dto.request.CategoryUpdateRequest;
import com.eventpro.dto.request.EventCreateRequest;
import com.eventpro.dto.response.CategoryResponse;
import com.eventpro.dto.response.EventResponse;
import com.eventpro.service.EvenementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Événements", description = "Gestion du catalogue des événements et des catégories de billets")
public class EvenementController {

    private final EvenementService evenementService;

    @PostMapping
    @Operation(summary = "Créer un nouvel événement en mode DRAFT")
    public ResponseEntity<EventResponse> creerEvenement(@Valid @RequestBody EventCreateRequest request) {
        EventResponse response = evenementService.creerEvenement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Consulter le catalogue public des événements publiés")
    public ResponseEntity<List<EventResponse>> getCataloguePublic() {
        return ResponseEntity.ok(evenementService.getCataloguePublic());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le détail d'un événement")
    public ResponseEntity<EventResponse> getEvenementById(@PathVariable Long id) {
        return ResponseEntity.ok(evenementService.getEvenementById(id));
    }

    @PutMapping("/{id}/publish")
    @Operation(summary = "Publier un événement pour ouvrir les consultations et ventes")
    public ResponseEntity<EventResponse> publierEvenement(@PathVariable Long id) {
        return ResponseEntity.ok(evenementService.publierEvenement(id));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Annuler un événement (ouvre droit au remboursement à 100%)")
    public ResponseEntity<EventResponse> annulerEvenement(@PathVariable Long id) {
        return ResponseEntity.ok(evenementService.annulerEvenement(id));
    }

    @PutMapping("/categories/{categoryId}")
    @Operation(summary = "Modifier une catégorie de billet avec contrôle strict de non-régression de capacité")
    public ResponseEntity<CategoryResponse> modifierCategorie(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryUpdateRequest request) {
        return ResponseEntity.ok(evenementService.modifierCategorie(categoryId, request));
    }

    @GetMapping("/organizer/{organisateurId}")
    @Operation(summary = "Lister les événements d'un organisateur")
    public ResponseEntity<List<EventResponse>> getEvenementsParOrganisateur(@PathVariable Long organisateurId) {
        return ResponseEntity.ok(evenementService.getEvenementsParOrganisateur(organisateurId));
    }
}
