package com.safetynet.alerts.service;

import com.safetynet.alerts.dto.*;
import com.safetynet.alerts.model.Firestation;
import com.safetynet.alerts.model.Person;
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
                .filter(m -> m.getFirstName().equals(p.getFirstName()) && m.getLastName().equals(p.getLastName()))
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
        List<Firestation> mappings = dataService.getFirestations().stream()
                .filter(fs -> fs.station != null && fs.station.equals(stationNumber))
                .collect(Collectors.toList());

        Set<String> addresses = mappings.stream()
                .map(fs -> fs.address)
                .collect(Collectors.toSet());

        List<Person> persons = dataService.getPersons().stream()
                .filter(p -> addresses.contains(p.getAddress()))
                .collect(Collectors.toList());

        List<ResidentInfoDto> personDtos = persons.stream()
                .map(p -> new ResidentInfoDto(p.getFirstName(), p.getLastName(), p.getAddress(), p.getPhone()))
                .collect(Collectors.toList());
        //  provide a count of the number of adults and the
        //  number of children (any individual aged 18 years or younger) in the served area.
        int children = 0;
        int adults = 0;
        for (Person p : persons) {
            Optional<Integer> ageOpt = findMedical(p).flatMap(m -> ageFromBirthdate(m.getBirthdate()));
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
                .filter(p -> p.getAddress().equalsIgnoreCase(address))
                .toList();
        // then find children among them
        List<ChildInfoDto> result = new ArrayList<>();

        for (Person p : residents) {
            Optional<Integer> ageOpt = findMedical(p).flatMap(m -> ageFromBirthdate(m.getBirthdate()));
            // make sure to only include children (age 18 or younger)
            if (ageOpt.isPresent() && ageOpt.get() <= 18) {
                List<ChildInfoDto.HouseholdMember> others = residents.stream()
                        .filter(o -> !(o.getFirstName().equals(p.getFirstName()) && o.getLastName().equals(p.getLastName())))
                        .map(o -> new ChildInfoDto.HouseholdMember(o.getFirstName(), o.getLastName()))
                        .collect(Collectors.toList());
                result.add(new ChildInfoDto(p.getFirstName(), p.getLastName(), ageOpt.get(), others));
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
                .filter(p -> addresses.contains(p.getAddress()))
                .map(Person::getPhone)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public FireAddressResponseDto getFire(String address) {
        Optional<Firestation> mapping = dataService.getFirestations().stream()
                .filter(fs -> fs.address.equalsIgnoreCase(address))
                .findFirst();

        String station = mapping.map(m -> m.station).orElse(null);

        List<Person> residents = dataService.getPersons().stream()
                .filter(p -> p.getAddress().equalsIgnoreCase(address))
                .collect(Collectors.toList());
        // return the list of residents living at the given address as well as the fire
        // station number serving the address. The list includes the name, phone number,
        // age, and medical history (medications and allergies) of each person
        List<ResidentInfoDto> residentDtos = residents.stream().map(p -> {
            Optional<MedicalRecord> mr = findMedical(p);
            int age = mr.flatMap(m -> ageFromBirthdate(m.getBirthdate())).orElse(0);
            List<String> meds = mr.map(MedicalRecord::getMedications).orElse(Collections.emptyList());
            List<String> allergies = mr.map(MedicalRecord::getAllergies).orElse(Collections.emptyList());
            return new ResidentInfoDto(p.getFirstName(), p.getLastName(), p.getPhone(), age, meds, allergies);
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
                .filter(p -> p.getAddress() != null && addresses.stream().anyMatch(addr -> addr.equalsIgnoreCase(p.getAddress())))
                .collect(Collectors.groupingBy(Person::getAddress));

        Map<String, List<ResidentInfoDto>> result = new HashMap<>();

        for (Map.Entry<String, List<Person>> entry : personsByAddress.entrySet()) {
            List<ResidentInfoDto> residentDtos = entry.getValue().stream().map(p -> {
                Optional<MedicalRecord> mr = findMedical(p);
                int age = mr.flatMap(m -> ageFromBirthdate(m.getBirthdate())).orElse(0);
                List<String> meds = mr.map(MedicalRecord::getMedications).orElse(Collections.emptyList());
                List<String> allergies = mr.map(MedicalRecord::getAllergies).orElse(Collections.emptyList());
                return new ResidentInfoDto(p.getFirstName(), p.getLastName(), p.getPhone(), age, meds, allergies);
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
                .filter(p -> p.getLastName() != null && p.getLastName().toLowerCase().equals(match))
                .map(p -> {
                    Optional<MedicalRecord> mr = dataService.getMedicalrecords().stream()
                            .filter(m -> m.getFirstName() != null && m.getLastName() != null
                                    && m.getFirstName().equalsIgnoreCase(p.getFirstName())
                                    && m.getLastName().equalsIgnoreCase(p.getLastName()))
                            .findFirst();

                    int age = mr.map(m -> computeAge(m.getBirthdate())).orElse(0);
                    List<String> meds = mr.map(MedicalRecord::getMedications).orElse(Collections.emptyList());
                    List<String> allergies = mr.map(MedicalRecord::getAllergies).orElse(Collections.emptyList());

                    return new ResidentInfoDto(
                            p.getFirstName(),
                            p.getLastName(),
                            p.getPhone(),
                            age,
                            p.getEmail(),
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
                .filter(p -> p.getCity() != null
                        // convert to lower case and compare the normalized string to match
                        && p.getCity().trim().toLowerCase(Locale.ROOT).equals(match)
                        && p.getEmail() != null
                        && !p.getEmail().isBlank())
                .map(Person::getEmail)
                .distinct()
                .collect(Collectors.toList());
    }

    public void addPerson(PersonDto dto) {
        if (dto == null || dto.firstName() == null || dto.lastName() == null) return;
        Person p = new Person(dto.firstName(), dto.lastName(), dto.address(), dto.city(), dto.zip(), dto.phone(), dto.email());
        dataService.getPersons().add(p);
    }

    public boolean updatePerson(PersonDto dto) {
        if (dto == null || dto.firstName() == null || dto.lastName() == null) return false;

        for (Person p : dataService.getPersons()) {
            if (p.getFirstName() != null && p.getLastName() != null && p.getFirstName().equals(dto.firstName()) && p.getLastName().equals(dto.lastName())) {
                p.setAddress(dto.address());
                p.setCity(dto.city());
                p.setZip(dto.zip());
                p.setPhone(dto.phone());
                p.setEmail(dto.email());
                return true;
            }
        }
        return false;
    }

    public boolean deletePerson(String firstName, String lastName) {
        if (firstName == null || lastName == null) return false;
        return dataService.getPersons().removeIf(p ->
                Objects.equals(p.getFirstName(), firstName) && Objects.equals(p.getLastName(), lastName));
    }

    public void addFirestation(FirestationDto dto) {
        if (dto == null || dto.getAddress() == null || dto.getStation() == null) return;

        Firestation f = new Firestation();
        // adjust if model uses setters
        f.setAddress(dto.getAddress());
        f.setStation(dto.getStation());
        dataService.getFirestations().add(f);
    }

    public boolean updateFirestation(FirestationDto dto) {
        if (dto == null || dto.getAddress() == null || dto.getStation() == null) return false;

        for (Firestation f : dataService.getFirestations()) {
            if (f.address != null && f.address.equals(dto.getAddress())) {
                f.station = dto.getStation();
                return true;
            }
        }
        return false;
    }

    public boolean deleteFirestation(String address, String stationNumber) {
        if ((address == null || address.isBlank()) && (stationNumber == null || stationNumber.isBlank())) {
            return false;
        }

        if (address != null) {
            return dataService.getFirestations().removeIf(f -> Objects.equals(f.address, address));
        } else {
            return dataService.getFirestations().removeIf(f -> Objects.equals(f.station, stationNumber));
        }
    }

    public void addMedicalRecord(ResidentInfoDto dto) {
        if (dto == null || dto.getFirstName() == null || dto.getLastName() == null) return;
        MedicalRecord m = new MedicalRecord(dto.getFirstName(), dto.getLastName(), dto.getBirthdate(), dto.getMedications() != null ? new ArrayList<>(dto.getMedications()) : new ArrayList<>(), dto.getAllergies() != null ? new ArrayList<>(dto.getAllergies()) : new ArrayList<>());
        dataService.getMedicalrecords().add(m);
    }

    public boolean updateMedicalRecord(ResidentInfoDto record) {
        if (record == null || record.getFirstName() == null || record.getLastName() == null) {
            return false;
        }

        List<MedicalRecord> medicalRecords = dataService.getMedicalrecords();
        if (medicalRecords == null) {
            return false;
        }

        for (MedicalRecord m : medicalRecords) {
            if (m.getFirstName() != null && m.getLastName() != null
                    && m.getFirstName().equals(record.getFirstName())
                    && m.getLastName().equals(record.getLastName())) {

                m.setBirthdate(record.getBirthdate());
                m.setMedications(record.getMedications() != null
                        ? new ArrayList<>(record.getMedications())
                        : new ArrayList<>());
                m.setAllergies(record.getMedications() != null
                        ? new ArrayList<>(record.getMedications())
                        : new ArrayList<>());
                return true;
            }
        }
        return false;
    }

    public boolean deleteMedicalRecord(String firstName, String lastName) {
        if (firstName == null || lastName == null || firstName.isBlank() || lastName.isBlank()) {
            return false;
        }
        List<MedicalRecord> medicalRecords = dataService.getMedicalrecords();
        if (medicalRecords == null) {
            return false;
        }
        return medicalRecords.removeIf(m ->
                Objects.equals(m.getFirstName(), firstName) && Objects.equals(m.getLastName(), lastName));
    }
}