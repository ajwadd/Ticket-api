package com.eventpro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "categories_billet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorieBillet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id", nullable = false)
    private Evenement evenement;

    @Column(nullable = false)
    private String nom;

    @Column(name = "prix_unitaire", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "quantite_totale", nullable = false)
    private int quantiteTotale;

    @Column(name = "places_reservees", nullable = false)
    @Builder.Default
    private int placesReservees = 0;

    @Column(name = "places_vendues", nullable = false)
    @Builder.Default
    private int placesVendues = 0;

    @Column(name = "quota_max_par_client", nullable = false)
    @Builder.Default
    private int quotaMaxParClient = 10;

    public int getPlacesDisponibles() {
        return Math.max(0, quantiteTotale - (placesReservees + placesVendues));
    }

    public boolean verifierStock(int quantiteDemandee) {
        return quantiteDemandee > 0 && getPlacesDisponibles() >= quantiteDemandee;
    }

    public void reserver(int quantite) {
        if (!verifierStock(quantite)) {
            throw new IllegalStateException("Stock insuffisant pour cette catégorie");
        }
        this.placesReservees += quantite;
    }

    public void libererReservation(int quantite) {
        this.placesReservees = Math.max(0, this.placesReservees - quantite);
    }

    public void confirmerVente(int quantite) {
        libererReservation(quantite);
        this.placesVendues += quantite;
    }
}
