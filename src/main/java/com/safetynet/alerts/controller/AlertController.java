package com.safetynet.alerts.controller;

import com.safetynet.alerts.dto.*;
import com.safetynet.alerts.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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

    // URLs
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

    @GetMapping("/personInfo")
    public List<ResidentInfoDto> getPersonInfo(
            @RequestParam("lastName") String lastName) {
        return alertService.getPersonInfoByLastName(lastName);
    }

    @GetMapping("/communityEmail")
    public List<String> getCommunityEmail(@RequestParam("city") String city) {
        return alertService.getEmailsByCity(city);
    }

    // endpoints
    @PostMapping("/person")
    public ResponseEntity<Void> addPerson(@RequestBody PersonDto person) {
        alertService.addPerson(person);
        URI location = URI.create("/person?firstName=" + person.firstName() + "&lastName=" + person.lastName());
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/person")
    public ResponseEntity<Void> updatePerson(@RequestBody PersonDto person) {
        boolean updated = alertService.updatePerson(person);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/person")
    public ResponseEntity<Void> deletePersonByBody(@RequestBody PersonDto person) {
        if (person == null || person.firstName() == null || person.lastName() == null) {
            return ResponseEntity.badRequest().build();
        }
        boolean deleted = alertService.deletePerson(person.firstName(), person.lastName());
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/firestation")
    public ResponseEntity<Void> addFirestation(@RequestBody(required = false) FirestationDto dto,
                                               UriComponentsBuilder uriBuilder) {
        if (dto == null
                || dto.getAddress() == null || dto.getAddress().isBlank()
                || dto.getStation() == null || dto.getStation().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        alertService.addFirestation(dto);

        URI location = uriBuilder
                .path("/firestation")
                .queryParam("address", dto.getAddress())
                .build()
                .encode()
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping("/firestation")
    public ResponseEntity<Void> updateFirestation(@RequestBody FirestationDto dto) {
        if (dto == null || dto.getAddress() == null || dto.getStation() == null) {
            return ResponseEntity.badRequest().build();
        }
        boolean updated = alertService.updateFirestation(dto);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/firestation")
    public ResponseEntity<Void> deleteFirestation(@RequestBody(required = false) FirestationDto dto) {
        if (dto == null) {
            return ResponseEntity.badRequest().build();
        }

        String station = dto.getStation();
        String address = dto.getAddress();

        // make sure either address or station is provided for deletion
        if ((station == null || station.isBlank()) && (address == null || address.isBlank())) {
            return ResponseEntity.badRequest().build();
        }

        boolean deleted = alertService.deleteFirestation(address, station);

        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}