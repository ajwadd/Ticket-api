package com.eventpro.service;

import com.eventpro.dto.request.RefundRequest;
import com.eventpro.dto.response.RefundResponse;

import java.util.List;

public interface RemboursementService {

    RefundResponse traiterDemandeRemboursement(RefundRequest request);

    List<RefundResponse> getRemboursementsParPaiement(Long paiementId);
}
