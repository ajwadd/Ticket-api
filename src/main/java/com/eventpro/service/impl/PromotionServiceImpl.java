package com.eventpro.service.impl;

import com.eventpro.dto.request.PromotionCreateRequest;
import com.eventpro.dto.response.PromotionResponse;
import com.eventpro.entity.Evenement;
import com.eventpro.entity.Organisateur;
import com.eventpro.entity.Promotion;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.exception.ValidationException;
import com.eventpro.mapper.PromotionMapper;
import com.eventpro.repository.EvenementRepository;
import com.eventpro.repository.OrganisateurRepository;
import com.eventpro.repository.PromotionRepository;
import com.eventpro.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final EvenementRepository evenementRepository;
    private final OrganisateurRepository organisateurRepository;
    private final PromotionMapper promotionMapper;

    @Override
    public PromotionResponse creerPromotion(PromotionCreateRequest request) {
        log.info("Création de la promotion : {}", request.getCode());

        if (promotionRepository.findByCode(request.getCode().trim().toUpperCase()).isPresent()) {
            throw new ValidationException("Un code promotionnel avec ce code existe déjà: " + request.getCode());
        }

        if (request.getDateDebut().isAfter(request.getDateFin())) {
            throw new ValidationException("La date de début doit être antérieure à la date de fin");
        }

        Promotion promo = promotionMapper.toPromotion(request);
        promo.setCode(request.getCode().trim().toUpperCase());
        promo.setNombreUtilisations(0);
        promo.setNombreReservees(0);

        if (request.getEvenementId() != null) {
            Evenement evenement = evenementRepository.findById(request.getEvenementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable avec l'ID: " + request.getEvenementId()));
            promo.setEvenement(evenement);
        }

        if (request.getOrganisateurId() != null) {
            Organisateur organisateur = organisateurRepository.findById(request.getOrganisateurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organisateur introuvable avec l'ID: " + request.getOrganisateurId()));
            promo.setOrganisateur(organisateur);
        }

        Promotion saved = promotionRepository.save(promo);
        return promotionMapper.toPromotionResponse(saved);
    }

    @Override
    public Promotion validerEtObtenirPromotion(String code, Evenement evenement, BigDecimal montantBrut) {
        if (code == null || code.isBlank()) {
            return null;
        }

        Promotion promo = promotionRepository.findByCodeWithLock(code.trim().toUpperCase())
                .orElseThrow(() -> new ValidationException("Code promotionnel invalide : " + code));

        if (!promo.estValide()) {
            throw new ValidationException("Le code promotionnel n'est plus valide ou a expiré : " + code);
        }

        // Vérification du périmètre : si la promo est rattachée à un événement, il doit correspondre
        if (promo.getEvenement() != null && !promo.getEvenement().getId().equals(evenement.getId())) {
            throw new ValidationException("Ce code promotionnel n'est pas applicable à cet événement");
        }

        if (promo.getQuotaDisponible() <= 0) {
            throw new ValidationException("Le quota d'utilisation du code promotionnel est épuisé");
        }

        return promo;
    }

    @Override
    public void reserverQuota(String code) {
        if (code == null || code.isBlank()) return;
        Promotion promo = promotionRepository.findByCodeWithLock(code.trim().toUpperCase())
                .orElseThrow(() -> new ValidationException("Code promotionnel introuvable : " + code));
        promo.reserverQuota();
        promotionRepository.save(promo);
    }

    @Override
    public void libererQuota(Promotion promotion) {
        if (promotion == null) return;
        promotion.libererQuota();
        promotionRepository.save(promotion);
    }

    @Override
    public void consommerQuota(Promotion promotion) {
        if (promotion == null) return;
        promotion.consommerQuota();
        promotionRepository.save(promotion);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponse getPromotionByCode(String code) {
        Promotion promo = promotionRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Code promotionnel introuvable : " + code));
        return promotionMapper.toPromotionResponse(promo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> getPromotionsGlobales() {
        return promotionMapper.toPromotionResponseList(promotionRepository.findByEvenementIsNull());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> getPromotionsParEvenement(Long evenementId) {
        return promotionMapper.toPromotionResponseList(promotionRepository.findByEvenementId(evenementId));
    }
}
