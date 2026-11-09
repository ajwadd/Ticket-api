package com.eventpro.entity;

import com.eventpro.enums.StatutEvenement;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evenements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String lieu;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;

    @Column(name = "date_ouverture_vente")
    private LocalDateTime dateOuvertureVente;

    @Column(name = "date_cloture_vente")
    private LocalDateTime dateClotureVente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutEvenement statut = StatutEvenement.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisateur_id")
    private Organisateur organisateur;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CategorieBillet> categories = new ArrayList<>();

    @OneToMany(mappedBy = "evenement")
    @Builder.Default
    private List<Promotion> promotions = new ArrayList<>();

    public void publier() {
        if (this.categories == null || this.categories.isEmpty()) {
            throw new IllegalStateException("Impossible de publier un événement sans catégorie de billet.");
        }
        this.statut = StatutEvenement.PUBLISHED;
    }

    public void annuler() {
        this.statut = StatutEvenement.CANCELLED;
    }

    public boolean verifierDisponibilite() {
        if (this.statut != StatutEvenement.PUBLISHED) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (dateOuvertureVente != null && now.isBefore(dateOuvertureVente)) {
            return false;
        }
        if (dateClotureVente != null && now.isAfter(dateClotureVente)) {
            return false;
        }
        return categories.stream().anyMatch(cat -> cat.getPlacesDisponibles() > 0);
    }
}
