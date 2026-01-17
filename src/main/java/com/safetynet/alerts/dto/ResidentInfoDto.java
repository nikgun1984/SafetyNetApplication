package com.safetynet.alerts.dto;

import java.util.List;

public class ResidentInfoDto {
    public String firstName;
    public String lastName;
    public String phone;
    public int age;
    public List<String> medications;
    public List<String> allergies;

    public ResidentInfoDto(String firstName, String lastName, String phone, int age,
                           List<String> medications, List<String> allergies) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.age = age;
        this.medications = medications;
        this.allergies = allergies;
    }
}