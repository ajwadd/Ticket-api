package com.eventpro.entity;

import com.eventpro.enums.StatutRemboursement;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "remboursements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Remboursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paiement_id", nullable = false)
    private Paiement paiement;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private String motif;

    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutRemboursement statut = StatutRemboursement.PENDING;

    public void demander() {
        this.dateDemande = LocalDateTime.now();
        this.statut = StatutRemboursement.PENDING;
    }

    public void approuver() {
        this.statut = StatutRemboursement.APPROVED;
    }

    public void refuser() {
        this.statut = StatutRemboursement.REJECTED;
    }
}
