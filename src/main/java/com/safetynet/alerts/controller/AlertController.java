package com.safetynet.alerts.controller;

import com.safetynet.alerts.dto.ChildInfoDto;
import com.safetynet.alerts.dto.FireAddressResponseDto;
import com.safetynet.alerts.dto.ResidentInfoDto;
import com.safetynet.alerts.service.AlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/firestation")
    public Map<String, Object> getFirestation(@RequestParam("stationNumber") String stationNumber) {
        return alertService.getFirestationPeople(stationNumber);
    }

    @GetMapping("/childAlert")
    public List<ChildInfoDto> getChildAlert(@RequestParam("address") String address) {
        return alertService.getChildAlert(address);
    }

    @GetMapping("/phoneAlert")
    public List<String> getPhoneAlert(@RequestParam("firestation") String stationNumber) {
        return alertService.getPhoneAlert(stationNumber);
    }

    @GetMapping("/fire")
    public FireAddressResponseDto getFire(@RequestParam("address") String address) {
        return alertService.getFire(address);
    }

    @GetMapping("/flood/stations")
    public Map<String, List<ResidentInfoDto>> getFloodStations(
            @RequestParam("stations") String stations) {

        List<String> stationList = Arrays.stream(stations.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        return alertService.getFloodStations(stationList);
    }
}