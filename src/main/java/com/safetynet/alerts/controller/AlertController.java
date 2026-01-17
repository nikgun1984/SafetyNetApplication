package com.safetynet.alerts.controller;

import com.safetynet.alerts.dto.ChildInfoDto;
import com.safetynet.alerts.service.AlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
}