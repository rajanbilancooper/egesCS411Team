package com.Eges411Team.UnifiedPatientManager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.*;

// tells us this is a controller class
@RestController
// base path for all patient-related requests
@RequestMapping("/patients")
public class PatientController {
    
    // the controller has no attributes for now
    public PatientController() {
        // no params for a controller since it just manages requests
    }

    
}