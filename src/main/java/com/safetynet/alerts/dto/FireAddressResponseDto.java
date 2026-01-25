package com.safetynet.alerts.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FireAddressResponseDto {
    public String station;
    public List<ResidentInfoDto> residents;

    public FireAddressResponseDto(String station, List<ResidentInfoDto> residents) {
        this.station = station;
        this.residents = residents;
    }
}
