package com.safetynet.alerts.dto;

import java.util.List;

public class FirestationResponseDto {
    public List<ResidentInfoDto> persons;
    public int adults;
    public int children;

    public FirestationResponseDto(List<ResidentInfoDto> persons, int adults, int children) {
        this.persons = persons;
        this.adults = adults;
        this.children = children;
    }
}