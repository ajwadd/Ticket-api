package com.eventpro.entity;

import com.eventpro.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "paiements", indexes = {
        @Index(name = "idx_paiement_idempotency", columnList = "idempotency_key")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Column(name = "date_paiement", nullable = false)
    private LocalDateTime datePaiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutPaiement statut = StatutPaiement.PENDING;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    @OneToMany(mappedBy = "paiement", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Remboursement> remboursements = new ArrayList<>();

    public void payer() {
        this.statut = StatutPaiement.SUCCESS;
        this.datePaiement = LocalDateTime.now();
    }

    public void rembourser(BigDecimal montantRembourse) {
        // Enregistrement d'un remboursement
    }
}
