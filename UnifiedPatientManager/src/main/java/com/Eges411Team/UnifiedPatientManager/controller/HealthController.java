package com.Eges411Team.UnifiedPatientManager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        // Simple static response; expand later with DB checks if needed
        return ResponseEntity.ok("OK");
    }
}
