package com.eventpro.entity;

import com.eventpro.enums.StatutBillet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "billets", indexes = {
        @Index(name = "idx_billet_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Billet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "date_emission", nullable = false)
    private LocalDateTime dateEmission;

    @Column(name = "date_controle")
    private LocalDateTime dateControle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutBillet statut = StatutBillet.VALID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ligne_reservation_id", nullable = false)
    private LigneReservation ligneReservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_billet_id", nullable = false)
    private CategorieBillet categorieBillet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id", nullable = false)
    private Evenement evenement;

    @Column(name = "nom_porteur")
    private String nomPorteur;

    public void valider() {
        this.statut = StatutBillet.VALID;
    }

    public void controler() {
        if (this.statut != StatutBillet.VALID) {
            throw new IllegalStateException("Le billet ne peut pas être composté (statut: " + this.statut + ")");
        }
        this.statut = StatutBillet.USED;
        this.dateControle = LocalDateTime.now();
    }
}
