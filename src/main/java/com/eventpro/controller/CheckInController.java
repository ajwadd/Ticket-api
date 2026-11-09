package com.eventpro.controller;

import com.eventpro.dto.request.CheckInRequest;
import com.eventpro.dto.response.CheckInResponse;
import com.eventpro.service.CheckInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/check-in")
@RequiredArgsConstructor
@Tag(name = "Contrôle d'Accès", description = "Vérification et compostage atomique des billets au point d'entrée")
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    @Operation(summary = "Scanner et composter un billet (transition atomique VALID vers USED)")
    public ResponseEntity<CheckInResponse> composterBillet(@Valid @RequestBody CheckInRequest request) {
        CheckInResponse response = checkInService.composterBillet(request);
        return ResponseEntity.ok(response);
    }
}
