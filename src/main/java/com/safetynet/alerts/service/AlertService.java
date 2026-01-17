package com.safetynet.alerts.service;

import com.safetynet.alerts.dto.ChildInfoDto;
import com.safetynet.alerts.dto.PersonInfoDto;
import com.safetynet.alerts.model.Person;
import com.safetynet.alerts.model.FireStationMapping;
import com.safetynet.alerts.model.MedicalRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlertService {
    private final DataService dataService;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public AlertService(DataService dataService) {
        this.dataService = dataService;
    }

    private Optional<MedicalRecord> findMedical(Person p) {
        return dataService.getMedicalrecords().stream()
                .filter(m -> m.firstName.equals(p.firstName) && m.lastName.equals(p.lastName))
                .findFirst();
    }

    private Optional<Integer> ageFromBirthdate(String birthdate) {
        try {
            LocalDate b = LocalDate.parse(birthdate, fmt);
            return Optional.of(Period.between(b, LocalDate.now()).getYears());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Map<String, Object> getFirestationPeople(String stationNumber) {
        List<FireStationMapping> mappings = dataService.getFirestations().stream()
                .filter(fs -> fs.station != null && fs.station.equals(stationNumber))
                .toList();

        Set<String> addresses = mappings.stream()
                .map(fs -> fs.address)
                .collect(Collectors.toSet());

        List<Person> persons = dataService.getPersons().stream()
                .filter(p -> addresses.contains(p.address))
                .toList();

        List<PersonInfoDto> personDtos = persons.stream()
                .map(p -> new PersonInfoDto(p.firstName, p.lastName, p.address, p.phone))
                .collect(Collectors.toList());

        int children = 0;
        int adults = 0;
        for (Person p : persons) {
            Optional<Integer> ageOpt = findMedical(p).flatMap(m -> ageFromBirthdate(m.birthdate));
            if (ageOpt.isPresent()) {
                int age = ageOpt.get();
                if (age <= 18) children++; else adults++;
            }
            // if no medical record / invalid birthdate -> do not count (avoids false child classification)
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("persons", personDtos);
        resp.put("children", children);
        resp.put("adults", adults);
        return resp;
    }

    public List<ChildInfoDto> getChildAlert(String address) {
        List<Person> residents = dataService.getPersons().stream()
                .filter(p -> p.address.equalsIgnoreCase(address))
                .toList();

        List<ChildInfoDto> result = new ArrayList<>();
        for (Person p : residents) {
            Optional<Integer> ageOpt = findMedical(p).flatMap(m -> ageFromBirthdate(m.birthdate));
            if (ageOpt.isPresent() && ageOpt.get() <= 18) {
                List<ChildInfoDto.HouseholdMember> others = residents.stream()
                        .filter(o -> !(o.firstName.equals(p.firstName) && o.lastName.equals(p.lastName)))
                        .map(o -> new ChildInfoDto.HouseholdMember(o.firstName, o.lastName))
                        .collect(Collectors.toList());
                result.add(new ChildInfoDto(p.firstName, p.lastName, ageOpt.get(), others));
            }
        }
        return result;
    }
}