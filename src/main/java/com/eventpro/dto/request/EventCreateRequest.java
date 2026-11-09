package com.eventpro.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class EventCreateRequest {

    @NotBlank(message = "Le titre de l'événement est obligatoire")
    private String titre;

    private String description;

    @NotBlank(message = "Le lieu est obligatoire")
    private String lieu;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime dateFin;

    private LocalDateTime dateOuvertureVente;

    private LocalDateTime dateClotureVente;

    @NotNull(message = "L'ID de l'organisateur est obligatoire")
    private Long organisateurId;

    @NotEmpty(message = "Au moins une catégorie de billet doit être définie")
    @Valid
    private List<CategoryCreateRequest> categories;
}
