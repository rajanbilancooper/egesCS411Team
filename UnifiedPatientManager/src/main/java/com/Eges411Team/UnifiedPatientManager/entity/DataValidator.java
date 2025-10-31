package com.Eges411Team.UnifiedPatientManager.entity;

import jakarta.persistence.*;

import java.util.Date;

public class DataValidator {

    // methods for the validation of different data types
    public static boolean isValidDate(Date date) {
        // Example validation: date should not be in the future
        return date.before(new Date());
    }

    public static boolean isValidString(String str) {
        // Example validation: string should not be null or empty
        return str != null && !str.trim().isEmpty();
    }
}