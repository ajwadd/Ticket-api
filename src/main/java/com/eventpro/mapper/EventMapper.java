package com.eventpro.mapper;

import com.eventpro.dto.request.CategoryCreateRequest;
import com.eventpro.dto.request.EventCreateRequest;
import com.eventpro.dto.response.CategoryResponse;
import com.eventpro.dto.response.EventResponse;
import com.eventpro.entity.CategorieBillet;
import com.eventpro.entity.Evenement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "organisateurId", source = "organisateur.id")
    @Mapping(target = "organisateurNom", source = "organisateur.nom")
    @Mapping(target = "categories", source = "categories")
    EventResponse toEventResponse(Evenement evenement);

    List<EventResponse> toEventResponseList(List<Evenement> evenements);

    @Mapping(target = "placesDisponibles", expression = "java(categorieBillet.getPlacesDisponibles())")
    CategoryResponse toCategoryResponse(CategorieBillet categorieBillet);

    List<CategoryResponse> toCategoryResponseList(List<CategorieBillet> categories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "organisateur", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    @Mapping(target = "categories", ignore = true)
    Evenement toEvenement(EventCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "evenement", ignore = true)
    @Mapping(target = "placesReservees", constant = "0")
    @Mapping(target = "placesVendues", constant = "0")
    CategorieBillet toCategorieBillet(CategoryCreateRequest request);
}
