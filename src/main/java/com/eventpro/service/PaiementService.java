package com.eventpro.service;

import com.eventpro.dto.request.PaymentRequest;
import com.eventpro.dto.response.PaymentResponse;

public interface PaiementService {

    PaymentResponse effectuerPaiement(String idempotencyKey, PaymentRequest request);

    PaymentResponse getPaiementByReference(String reference);

    PaymentResponse getPaiementByReservation(String reservationReference);
}
