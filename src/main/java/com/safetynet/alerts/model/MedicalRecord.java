package com.safetynet.alerts.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicalRecord {
    private String firstName;
    private String lastName;
    private String birthdate; // expected format: MM/dd/yyyy
    private List<String> medications;
    private List<String> allergies;
}
