package com.eventpro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lignes_reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_billet_id", nullable = false)
    private CategorieBillet categorieBillet;

    @Column(nullable = false)
    private int quantite;

    @Column(name = "prix_unitaire", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "sous_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal sousTotal;

    @OneToMany(mappedBy = "ligneReservation", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Billet> billets = new ArrayList<>();

    public BigDecimal calculerSousTotal() {
        if (prixUnitaire == null) {
            prixUnitaire = BigDecimal.ZERO;
        }
        this.sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        return this.sousTotal;
    }
}
