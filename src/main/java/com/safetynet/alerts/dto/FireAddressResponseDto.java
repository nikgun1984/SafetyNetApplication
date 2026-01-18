package com.safetynet.alerts.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class FireAddressResponseDto {
    public String station;
    public List<ResidentInfoDto> residents;

    public FireAddressResponseDto(String station, List<ResidentInfoDto> residents) {
        this.station = station;
        this.residents = residents;
    }
}
