package com.eventpro.mapper;

import com.eventpro.dto.response.LigneReservationResponse;
import com.eventpro.dto.response.ReservationResponse;
import com.eventpro.entity.LigneReservation;
import com.eventpro.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "evenementId", source = "evenement.id")
    @Mapping(target = "titreEvenement", source = "evenement.titre")
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientNom", source = "client.nom")
    @Mapping(target = "montantNet", source = "montantTotal")
    @Mapping(target = "codePromoApplique", source = "promotion.code")
    @Mapping(target = "lignes", source = "lignes")
    ReservationResponse toReservationResponse(Reservation reservation);

    List<ReservationResponse> toReservationResponseList(List<Reservation> reservations);

    @Mapping(target = "categorieId", source = "categorieBillet.id")
    @Mapping(target = "categorieNom", source = "categorieBillet.nom")
    LigneReservationResponse toLigneReservationResponse(LigneReservation ligneReservation);

    List<LigneReservationResponse> toLigneReservationResponseList(List<LigneReservation> lignes);
}
