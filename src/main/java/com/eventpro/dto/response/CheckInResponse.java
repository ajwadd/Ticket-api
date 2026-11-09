package com.eventpro.dto.response;

import com.eventpro.enums.StatutBillet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInResponse {
    private boolean success;
    private String message;
    private String codeUnique;
    private StatutBillet statut;
    private LocalDateTime dateControle;
    private String nomPorteur;
    private String categorieNom;
}
