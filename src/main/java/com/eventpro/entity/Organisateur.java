package com.eventpro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "organisateurs")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Organisateur extends Utilisateur {

    @Column(name = "nom_organisation")
    private String nomOrganisation;

    public void creerEvenement() {
    }

    public void modifierEvenement() {
    }
}
