package com.eventpro.dto.response;

import com.eventpro.enums.StatutBillet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {
    private Long id;
    private String codeUnique;
    private StatutBillet statut;
    private String nomPorteur;
    private String categorieNom;
    private String titreEvenement;
    private LocalDateTime dateEmission;
    private LocalDateTime dateControle;
    private String qrCodeBase64;
}
