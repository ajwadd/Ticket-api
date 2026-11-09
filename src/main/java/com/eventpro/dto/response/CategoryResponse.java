package com.eventpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Long id;
    private String nom;
    private BigDecimal prixUnitaire;
    private int quantiteTotale;
    private int placesReservees;
    private int placesVendues;
    private int placesDisponibles;
    private int quotaMaxParClient;
}
