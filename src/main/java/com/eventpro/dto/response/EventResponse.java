package com.eventpro.dto.response;

import com.eventpro.enums.StatutEvenement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {
    private Long id;
    private String titre;
    private String description;
    private String lieu;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private LocalDateTime dateOuvertureVente;
    private LocalDateTime dateClotureVente;
    private StatutEvenement statut;
    private Long organisateurId;
    private String organisateurNom;
    private List<CategoryResponse> categories;
}
