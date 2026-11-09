package com.eventpro.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "administrateurs")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Administrateur extends Utilisateur {

    public void gererUtilisateurs() {
        // Supervision globale
    }
}
