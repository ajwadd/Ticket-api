package com.eventpro.service;

import com.eventpro.dto.request.ReservationRequest;
import com.eventpro.dto.response.ReservationResponse;

import java.util.List;

public interface ReservationService {

    ReservationResponse creerReservation(ReservationRequest request);

    ReservationResponse getReservationByReference(String reference);

    ReservationResponse annulerReservation(String reference);

    List<ReservationResponse> getReservationsParClient(Long clientId);

    List<ReservationResponse> getReservationsParEvenement(Long evenementId);
}
