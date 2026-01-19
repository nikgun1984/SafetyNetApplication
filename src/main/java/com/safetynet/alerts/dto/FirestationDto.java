package com.safetynet.alerts.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FirestationDto {
    private String address;
    private String station;

    public FirestationDto() {
    }

    public FirestationDto(String address, String station) {
        this.address = address;
        this.station = station;
    }
}
