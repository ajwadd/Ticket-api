package com.eventpro.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "agents_controle")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class AgentControle extends Utilisateur {

    public boolean verifierBillet(String code) {
        return code != null && !code.isBlank();
    }
}
