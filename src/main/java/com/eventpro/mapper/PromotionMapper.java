package com.eventpro.mapper;

import com.eventpro.dto.request.PromotionCreateRequest;
import com.eventpro.dto.response.PromotionResponse;
import com.eventpro.entity.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @Mapping(target = "evenementId", source = "evenement.id")
    @Mapping(target = "quotaDisponible", expression = "java(promotion.getQuotaDisponible())")
    PromotionResponse toPromotionResponse(Promotion promotion);

    List<PromotionResponse> toPromotionResponseList(List<Promotion> promotions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nombreUtilisations", constant = "0")
    @Mapping(target = "nombreReservees", constant = "0")
    @Mapping(target = "evenement", ignore = true)
    @Mapping(target = "organisateur", ignore = true)
    Promotion toPromotion(PromotionCreateRequest request);
}
