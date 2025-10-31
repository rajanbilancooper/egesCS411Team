package com.Eges411Team.UnifiedPatientManager.controller;

import org.springframework.stereotype.Controller;

@Controller
public class DoctorController {
    // Controller methods for handling doctor-related requests will go here

    public String getDoctorDetails() {
        // Logic to retrieve and return doctor details
        return "doctorDetails";
    }
}