package com.safetynet.alerts.controller;

import com.safetynet.alerts.dto.*;
import com.safetynet.alerts.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for SafetyNet Alerts application.
 * <p>
 * This controller exposes endpoints for emergency services to:
 * <ul>
 *   <li>Retrieve information about residents covered by fire stations</li>
 *   <li>Get children at specific addresses with household information</li>
 *   <li>Access phone numbers for emergency SMS alerts</li>
 *   <li>Query fire station coverage and flood information</li>
 *   <li>Look up person details with medical history</li>
 *   <li>Manage CRUD operations for persons, fire stations, and medical records</li>
 * </ul>
 * </p>
 * <p>
 * All endpoints return JSON responses. Query endpoints use GET requests,
 * while resource management uses POST, PUT, and DELETE operations with
 * automatic data persistence.
 * </p>
 * 
 */
@RestController
public class AlertController {

    private static final Logger log = LoggerFactory.getLogger(AlertController.class);
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
        log.info("Created person {} {}", person.firstName(), person.lastName());
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/person")
    public ResponseEntity<Void> updatePerson(@RequestBody PersonDto person) {
        boolean updated = alertService.updatePerson(person);
        if (updated) {
            log.info("Updated person {} {}", person.firstName(), person.lastName());
            return ResponseEntity.ok().build();
        } else {
            log.info("Person not found for update: {} {}", person.firstName(), person.lastName());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/person")
    public ResponseEntity<Void> deletePersonByBody(@RequestBody PersonDto person) {
        if (person == null || person.firstName() == null || person.lastName() == null) {
            log.warn("Bad request to DELETE /person - missing firstName or lastName");
            return ResponseEntity.badRequest().build();
        }
        boolean deleted = alertService.deletePerson(person.firstName(), person.lastName());
        if (deleted) {
            log.info("Deleted person {} {}", person.firstName(), person.lastName());
            return ResponseEntity.ok().build();
        } else {
            log.info("Person not found for deletion: {} {}", person.firstName(), person.lastName());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/firestation")
    public ResponseEntity<Void> addFirestation(@RequestBody(required = false) FirestationDto dto,
                                               UriComponentsBuilder uriBuilder) {
        if (dto == null
                || dto.getAddress() == null || dto.getAddress().isBlank()
                || dto.getStation() == null || dto.getStation().isBlank()) {
            log.warn("Invalid firestation create request");
            return ResponseEntity.badRequest().build();
        }

        alertService.addFirestation(dto);
        log.info("Added firestation mapping address={} station={}", dto.getAddress(), dto.getStation());

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
            log.warn("Bad request to PUT /firestation - missing address or station");
            return ResponseEntity.badRequest().build();
        }
        boolean updated = alertService.updateFirestation(dto);
        if (updated) {
            log.info("Updated firestation for address={} to station={}", dto.getAddress(), dto.getStation());
            return ResponseEntity.ok().build();
        } else {
            log.info("Firestation not found for update: address={}", dto.getAddress());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/firestation")
    public ResponseEntity<Void> deleteFirestation(@RequestBody(required = false) FirestationDto dto) {
        if (dto == null) {
            log.warn("Bad request to DELETE /firestation - body is null");
            return ResponseEntity.badRequest().build();
        }

        String station = dto.getStation();
        String address = dto.getAddress();

        // make sure either address or station is provided for deletion
        if ((station == null || station.isBlank()) && (address == null || address.isBlank())) {
            log.warn("Bad request to DELETE /firestation - both address and station are missing or blank");
            return ResponseEntity.badRequest().build();
        }

        boolean deleted = alertService.deleteFirestation(address, station);

        if (deleted) {
            log.info("Deleted firestation mapping address={} station={}", address, station);
            return ResponseEntity.ok().build();
        } else {
            log.info("Firestation mapping not found for deletion address={} station={}", address, station);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/medicalRecord")
    public ResponseEntity<Void> addMedicalRecord(@RequestBody(required = false) ResidentInfoDto record,
                                                 UriComponentsBuilder uriBuilder) {
        if (record == null
                || record.getFirstName() == null || record.getFirstName().isBlank()
                || record.getLastName() == null || record.getLastName().isBlank()
                || record.getBirthdate() == null || record.getBirthdate().isBlank()) {
            log.warn("Invalid medical record create request");
            return ResponseEntity.badRequest().build();
        }
        alertService.addMedicalRecord(record);
        URI location = uriBuilder
                .path("/medicalRecord")
                .queryParam("firstName", record.getFirstName())
                .queryParam("lastName", record.getLastName())
                .build()
                .encode()
                .toUri();
        log.info("Created medical record for {} {}", record.getFirstName(), record.getLastName());
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/medicalRecord")
    public ResponseEntity<Void> updateMedicalRecord(@RequestBody ResidentInfoDto record) {
        if (record == null || record.getFirstName() == null || record.getLastName() == null) {
            log.warn("Bad request to PUT /medicalRecord - missing firstName or lastName");
            return ResponseEntity.badRequest().build();
        }
        boolean updated = alertService.updateMedicalRecord(record);
        if (updated) {
            log.info("Updated medical record for {} {}", record.getFirstName(), record.getLastName());
            return ResponseEntity.ok().build();
        } else {
            log.info("Medical record not found for update: {} {}", record.getFirstName(), record.getLastName());
            return ResponseEntity.notFound().build();
        }
    }
    // PathParams
    @DeleteMapping("/medicalRecord")
    public ResponseEntity<Void> deleteMedicalRecord(@RequestBody(required = false) ResidentInfoDto record) {
        if (record.getFirstName() == null || record.getLastName() == null || record.getFirstName().isBlank() || record.getLastName().isBlank()) {
            log.warn("Missing firstName/lastName for DELETE /medicalRecord");
            return ResponseEntity.badRequest().build();
        }
        boolean deleted = alertService.deleteMedicalRecord(record.getFirstName(), record.getLastName());
        if (deleted) {
            log.info("Deleted medical record {} {}", record.getFirstName(), record.getLastName());
            return ResponseEntity.ok().build();
        } else {
            log.info("Medical record not found for deletion: {} {}", record.getFirstName(), record.getLastName());
            return ResponseEntity.notFound().build();
        }
    }
}