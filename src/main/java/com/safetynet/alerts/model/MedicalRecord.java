package com.safetynet.alerts.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicalRecord {
    public String firstName;
    public String lastName;
    public String birthdate; // expected format: MM/dd/yyyy
    public List<String> medications;
    public List<String> allergies;
}
