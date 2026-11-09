package com.eventpro.entity;

import com.eventpro.enums.TypeReduction;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_reduction", nullable = false)
    private TypeReduction typeReduction;

    @Column(name = "valeur_reduction", nullable = false, precision = 12, scale = 2)
    private BigDecimal valeurReduction;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;

    @Column(name = "limite_utilisation", nullable = false)
    private int limiteUtilisation;

    @Column(name = "nombre_utilisations", nullable = false)
    @Builder.Default
    private int nombreUtilisations = 0;

    @Column(name = "nombre_reservees", nullable = false)
    @Builder.Default
    private int nombreReservees = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id")
    private Evenement evenement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisateur_id")
    private Organisateur organisateur;

    public int getQuotaDisponible() {
        return Math.max(0, limiteUtilisation - (nombreReservees + nombreUtilisations));
    }

    public boolean estValide() {
        LocalDateTime now = LocalDateTime.now();
        boolean dateOk = (dateDebut == null || !now.isBefore(dateDebut)) &&
                         (dateFin == null || !now.isAfter(dateFin));
        return dateOk && getQuotaDisponible() > 0;
    }

    public BigDecimal calculerReduction(BigDecimal montantBrut) {
        if (montantBrut == null || montantBrut.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal remise;
        if (typeReduction == TypeReduction.PERCENTAGE) {
            remise = montantBrut.multiply(valeurReduction)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            remise = valeurReduction.min(montantBrut).setScale(2, RoundingMode.HALF_UP);
        }
        return remise;
    }

    public void reserverQuota() {
        if (getQuotaDisponible() <= 0) {
            throw new IllegalStateException("Le quota pour ce code promotionnel est épuisé.");
        }
        this.nombreReservees++;
    }

    public void libererQuota() {
        this.nombreReservees = Math.max(0, this.nombreReservees - 1);
    }

    public void consommerQuota() {
        libererQuota();
        this.nombreUtilisations++;
    }
}
