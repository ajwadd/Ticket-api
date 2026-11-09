package com.eventpro.service;

import com.eventpro.dto.request.PromotionCreateRequest;
import com.eventpro.dto.response.PromotionResponse;
import com.eventpro.entity.Evenement;
import com.eventpro.entity.Promotion;

import java.math.BigDecimal;
import java.util.List;

public interface PromotionService {

    PromotionResponse creerPromotion(PromotionCreateRequest request);

    Promotion validerEtObtenirPromotion(String code, Evenement evenement, BigDecimal montantBrut);

    void reserverQuota(String code);

    void libererQuota(Promotion promotion);

    void consommerQuota(Promotion promotion);

    PromotionResponse getPromotionByCode(String code);

    List<PromotionResponse> getPromotionsGlobales();

    List<PromotionResponse> getPromotionsParEvenement(Long evenementId);
}
