package com.eventpro.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRequest {

    @NotNull(message = "L'identifiant de l'événement est obligatoire")
    private Long evenementId;

    @NotNull(message = "L'identifiant du client est obligatoire")
    private Long clientId;

    @NotEmpty(message = "La réservation doit comporter au moins une ligne")
    @Valid
    private List<LigneReservationRequest> lignes;

    private String codePromo;
}
