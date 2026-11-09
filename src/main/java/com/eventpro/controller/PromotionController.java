package com.eventpro.controller;

import com.eventpro.dto.request.PromotionCreateRequest;
import com.eventpro.dto.response.PromotionResponse;
import com.eventpro.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotions", description = "Gestion des codes promotionnels globaux ou par événement")
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    @Operation(summary = "Créer un nouveau code promotionnel (Admin ou Organisateur)")
    public ResponseEntity<PromotionResponse> creerPromotion(@Valid @RequestBody PromotionCreateRequest request) {
        PromotionResponse response = promotionService.creerPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Consulter le détail et quota disponible d'un code promo")
    public ResponseEntity<PromotionResponse> getPromotionByCode(@PathVariable String code) {
        return ResponseEntity.ok(promotionService.getPromotionByCode(code));
    }

    @GetMapping("/global")
    @Operation(summary = "Lister les promotions globales de la plateforme")
    public ResponseEntity<List<PromotionResponse>> getPromotionsGlobales() {
        return ResponseEntity.ok(promotionService.getPromotionsGlobales());
    }

    @GetMapping("/event/{evenementId}")
    @Operation(summary = "Lister les promotions rattachées à un événement spécifique")
    public ResponseEntity<List<PromotionResponse>> getPromotionsParEvenement(@PathVariable Long evenementId) {
        return ResponseEntity.ok(promotionService.getPromotionsParEvenement(evenementId));
    }
}
