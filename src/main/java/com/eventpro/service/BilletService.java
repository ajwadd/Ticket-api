package com.eventpro.service;

import com.eventpro.dto.response.TicketResponse;
import com.eventpro.entity.Billet;
import com.eventpro.entity.Reservation;

import java.util.List;

public interface BilletService {

    List<TicketResponse> genererBilletsPourReservation(Reservation reservation);

    TicketResponse getBilletByCode(String code);

    List<TicketResponse> getBilletsParReservation(Long reservationId);

    List<TicketResponse> getBilletsParEvenement(Long evenementId);
}
