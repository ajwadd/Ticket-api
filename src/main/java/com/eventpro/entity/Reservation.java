package com.eventpro.entity;

import com.eventpro.enums.StatutReservation;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutReservation statut = StatutReservation.PENDING_PAYMENT;

    @Column(name = "montant_brut", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantBrut = BigDecimal.ZERO;

    @Column(name = "montant_reduction", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantReduction = BigDecimal.ZERO;

    @Column(name = "montant_total", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id", nullable = false)
    private Evenement evenement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneReservation> lignes = new ArrayList<>();

    @OneToOne(mappedBy = "reservation")
    private Paiement paiement;

    public void calculerTotal() {
        this.montantBrut = lignes.stream()
                .map(LigneReservation::calculerSousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (promotion != null && promotion.estValide()) {
            this.montantReduction = promotion.calculerReduction(this.montantBrut);
        } else {
            this.montantReduction = BigDecimal.ZERO;
        }

        this.montantTotal = this.montantBrut.subtract(this.montantReduction).max(BigDecimal.ZERO);
    }

    public void confirmer() {
        this.statut = StatutReservation.CONFIRMED;
    }

    public void annuler() {
        this.statut = StatutReservation.CANCELLED;
    }

    public void expirer() {
        this.statut = StatutReservation.EXPIRED;
    }
}
