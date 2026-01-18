package com.safetynet.alerts.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResidentInfoDto {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String address;
    private int age;
    private List<String> medications;
    private List<String> allergies;

    public ResidentInfoDto() {}

    public ResidentInfoDto(String firstName, String lastName, String phone, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.age = age;
    }

    public ResidentInfoDto(String firstName, String lastName, String address, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phone = phone;
    }

    public ResidentInfoDto(String firstName, String lastName, String phone, int age,
                           List<String> medications, List<String> allergies) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.age = age;
        this.medications = medications;
        this.allergies = allergies;
    }

    public ResidentInfoDto(String firstName, String lastName, String address, int age, String email,
                                 List<String> medications, List<String> allergies) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.age = age;
        this.email = email;
        this.medications = medications;
        this.allergies = allergies;
    }
}