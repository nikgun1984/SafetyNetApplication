package com.safetynet.alerts.service;

import com.safetynet.alerts.dto.ChildInfoDto;
import com.safetynet.alerts.dto.FireAddressResponseDto;
import com.safetynet.alerts.dto.ResidentInfoDto;
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
                .collect(Collectors.toList());

        Set<String> addresses = mappings.stream()
                .map(fs -> fs.address)
                .collect(Collectors.toSet());

        List<Person> persons = dataService.getPersons().stream()
                .filter(p -> addresses.contains(p.address))
                .collect(Collectors.toList());

        List<ResidentInfoDto> personDtos = persons.stream()
                .map(p -> new ResidentInfoDto(p.firstName, p.lastName, p.address, p.phone))
                .collect(Collectors.toList());
        //  provide a count of the number of adults and the
        //  number of children (any individual aged 18 years or younger) in the served area.
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
        // filter residents at the given address
        List<Person> residents = dataService.getPersons().stream()
                .filter(p -> p.address.equalsIgnoreCase(address))
                .toList();
        // then find children among them
        List<ChildInfoDto> result = new ArrayList<>();

        for (Person p : residents) {
            Optional<Integer> ageOpt = findMedical(p).flatMap(m -> ageFromBirthdate(m.birthdate));
            // make sure to only include children (age 18 or younger)
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

    public List<String> getPhoneAlert(String stationNumber) {
        // get addresses covered by the station number
        Set<String> addresses = dataService.getFirestations().stream()
                .filter(fs -> fs.station != null && fs.station.equals(stationNumber))
                .map(fs -> fs.address)
                .collect(Collectors.toSet());
        // a list of phone numbers of residents served by the fire station
        return dataService.getPersons().stream()
                .filter(p -> addresses.contains(p.address))
                .map(p -> p.phone)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public FireAddressResponseDto getFire(String address) {
        Optional<FireStationMapping> mapping = dataService.getFirestations().stream()
                .filter(fs -> fs.address.equalsIgnoreCase(address))
                .findFirst();

        String station = mapping.map(m -> m.station).orElse(null);

        List<Person> residents = dataService.getPersons().stream()
                .filter(p -> p.address.equalsIgnoreCase(address))
                .collect(Collectors.toList());
        // return the list of residents living at the given address as well as the fire
        // station number serving the address. The list includes the name, phone number,
        // age, and medical history (medications and allergies) of each person
        List<ResidentInfoDto> residentDtos = residents.stream().map(p -> {
            Optional<MedicalRecord> mr = findMedical(p);
            int age = mr.flatMap(m -> ageFromBirthdate(m.birthdate)).orElse(0);
            List<String> meds = mr.map(m -> m.medications).orElse(Collections.emptyList());
            List<String> allergies = mr.map(m -> m.allergies).orElse(Collections.emptyList());
            return new ResidentInfoDto(p.firstName, p.lastName, p.phone, age, meds, allergies);
        }).collect(Collectors.toList());

        return new FireAddressResponseDto(station, residentDtos);
    }

    public Map<String, List<ResidentInfoDto>> getFloodStations(List<String> stationList) {
        if (stationList == null || stationList.isEmpty()) {
            return Collections.emptyMap();
        }

        // addresses served by the requested stations
        Set<String> addresses = dataService.getFirestations().stream()
                .filter(fs -> fs.station != null && stationList.contains(fs.station))
                .map(fs -> fs.address)
                .collect(Collectors.toSet());

        if (addresses.isEmpty()) {
            return Collections.emptyMap();
        }

        // group persons by their address (use person's address string as the map key)
        Map<String, List<Person>> personsByAddress = dataService.getPersons().stream()
                .filter(p -> p.address != null && addresses.stream().anyMatch(addr -> addr.equalsIgnoreCase(p.address)))
                .collect(Collectors.groupingBy(p -> p.address));

        Map<String, List<ResidentInfoDto>> result = new HashMap<>();

        for (Map.Entry<String, List<Person>> entry : personsByAddress.entrySet()) {
            List<ResidentInfoDto> residentDtos = entry.getValue().stream().map(p -> {
                Optional<MedicalRecord> mr = findMedical(p);
                int age = mr.flatMap(m -> ageFromBirthdate(m.birthdate)).orElse(0);
                List<String> meds = mr.map(m -> m.medications).orElse(Collections.emptyList());
                List<String> allergies = mr.map(m -> m.allergies).orElse(Collections.emptyList());
                return new ResidentInfoDto(p.firstName, p.lastName, p.phone, age, meds, allergies);
            }).collect(Collectors.toList());

            result.put(entry.getKey(), residentDtos);
        }

        return result;
    }

    public List<ResidentInfoDto> getPersonInfoByLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String match = lastName.trim().toLowerCase();

        List<Person> persons = dataService.getPersons();

        return persons.stream()
                .filter(p -> p.lastName != null && p.lastName.toLowerCase().equals(match))
                .map(p -> {
                    Optional<MedicalRecord> mr = dataService.getMedicalrecords().stream()
                            .filter(m -> m.firstName != null && m.lastName != null
                                    && m.firstName.equalsIgnoreCase(p.firstName)
                                    && m.lastName.equalsIgnoreCase(p.lastName))
                            .findFirst();

                    int age = mr.map(m -> computeAge(m.birthdate)).orElse(0);
                    List<String> meds = mr.map(m -> m.medications).orElse(Collections.emptyList());
                    List<String> allergies = mr.map(m -> m.allergies).orElse(Collections.emptyList());

                    return new ResidentInfoDto(
                            p.firstName,
                            p.lastName,
                            p.address,
                            age,
                            p.email,
                            meds,
                            allergies
                    );
                })
                .collect(Collectors.toList());
    }

    private int computeAge(String birthdate) {
        if (birthdate == null || birthdate.isBlank()) {
            return 0;
        }
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate bdate = LocalDate.parse(birthdate, fmt);
            return Period.between(bdate, LocalDate.now()).getYears();
        } catch (Exception e) {
            return 0;
        }
    }

    public List<String> getEmailsByCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String match = city.trim().toLowerCase(Locale.ROOT);

        return dataService.getPersons().stream()
                .filter(p -> p.city != null
                        // convert to lower case and compare the normalized string to match
                        && p.city.trim().toLowerCase(Locale.ROOT).equals(match)
                        && p.email != null
                        && !p.email.isBlank())
                .map(p -> p.email)
                .distinct()
                .collect(Collectors.toList());
    }
}