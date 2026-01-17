package com.safetynet.alerts.dto;

import java.util.List;

public class FirestationResponseDto {
    public List<PersonInfoDto> persons;
    public int adults;
    public int children;

    public FirestationResponseDto(List<PersonInfoDto> persons, int adults, int children) {
        this.persons = persons;
        this.adults = adults;
        this.children = children;
    }
}