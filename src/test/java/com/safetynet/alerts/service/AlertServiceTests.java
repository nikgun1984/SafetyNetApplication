package com.safetynet.alerts.service;

import com.safetynet.alerts.model.Firestation;
import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AlertServiceTests {
    @Mock
    private DataService dataService;

    private AlertService alertService;

    private List<Person> persons;
    private List<MedicalRecord> medicalRecords;
    private List<Firestation> firestations;

    @BeforeEach
    void setup() {
        persons = new ArrayList<>();
        medicalRecords = new ArrayList<>();
        firestations = new ArrayList<>();

        persons.add(new Person("Nick", "Gundobin", "1509 Highland Oaks Dr", "Tampa", "34638", "305-874-6512", "nick.gundobin@mail.com"));
        persons.add(new Person("Jane", "Gundobin", "1509 Highland Oaks Dr", "Tampa", "34638", "305-874-6513", "jane.gundobin@mail.com"));
        // child moved to the other address so station 3 has only the two adults above
        persons.add(new Person("Kid", "Young", "29 15th St", "Tampa", "34638", "305-874-6514", "kid.young@mail.com"));

        medicalRecords.add(new MedicalRecord("Nick", "Gundobin", "03/06/1984", List.of("med1"), List.of("nuts")));
        medicalRecords.add(new MedicalRecord("Jane", "Gundobin", "01/01/1980", List.of(), List.of()));
        medicalRecords.add(new MedicalRecord("Kid", "Young", "01/01/2012", List.of("med2"), List.of("peanuts")));

        Firestation f1 = new Firestation();
        f1.setAddress("1509 Highland Oaks Dr");
        f1.setStation("3");
        Firestation f2 = new Firestation();
        f2.setAddress("29 15th St");
        f2.setStation("2");

        firestations.add(f1);
        firestations.add(f2);

        when(dataService.getPersons()).thenReturn(persons);
        when(dataService.getMedicalrecords()).thenReturn(medicalRecords);
        when(dataService.getFirestations()).thenReturn(firestations);

        alertService = new AlertService(dataService);
    }

    @Test
    void getFirestationPeople_shouldReturnPersonsAndCounts() {
        Map<String, Object> resp = alertService.getFirestationPeople("3");
        assertNotNull(resp);
        assertTrue(resp.containsKey("persons"));
        List<?> list = (List<?>) resp.get("persons");
        assertEquals(2, list.size());

        Integer children = (Integer) resp.get("children");
        Integer adults = (Integer) resp.get("adults");
        assertNotNull(children);
        assertNotNull(adults);

        assertEquals(0, children.intValue()); // no kids at this address
        assertEquals(2, adults.intValue());
    }

    @Test
    void getChildAlert_shouldReturnChildrenAtAddress() {
        List<?> result = alertService.getChildAlert("29 15th St");
        assertNotNull(result);
        assertEquals(1, result.size()); // one kid here
    }

    @Test
    void getPhoneAlert_shouldReturnDistinctPhonesForStation() {
        List<String> phones = alertService.getPhoneAlert("3");
        assertNotNull(phones);
        assertEquals(2, phones.size());
        assertTrue(phones.contains("305-874-6512"));
        assertTrue(phones.contains("305-874-6513"));
    }

    @Test
    void getFire_shouldReturnStationAndResidentDetails() {
        var resp = alertService.getFire("1509 Highland Oaks Dr");
        assertNotNull(resp);
        assertEquals("3", resp.getStation());
        assertNotNull(resp.getResidents());
        assertEquals(2, resp.getResidents().size());

        boolean johnFound = resp.getResidents().stream().anyMatch(r ->
                "Nick".equals(r.getFirstName()) && r.getMedications() != null && r.getMedications().size() == 1);
        assertTrue(johnFound);
    }

    @Test
    void getFloodStations_shouldGroupByAddress() {
        List<String> stations = List.of("3", "2");
        var map = alertService.getFloodStations(stations);
        assertNotNull(map);
        // addresses returned should include both addresses used by our persons
        assertTrue(map.containsKey("1509 Highland Oaks Dr"));
        assertTrue(map.containsKey("29 15th St"));
    }

    @Test
    void getPersonInfoByLastName_shouldReturnMatchingPersonInfo() {
        var infos = alertService.getPersonInfoByLastName("Gundobin");
        assertNotNull(infos);
        // Nick and Jane Gundobin
        assertEquals(2, infos.size());
        boolean hasEmail = infos.stream().anyMatch(i -> "nick.gundobin@mail.com".equals(i.getEmail()));
        assertTrue(hasEmail);
    }

    @Test
    void getEmailsByCity_shouldReturnDistinctEmails() {
        var emails = alertService.getEmailsByCity("Tampa");
        assertNotNull(emails);
        // three persons with emails in the same city
        assertEquals(3, emails.size());
        assertTrue(emails.contains("nick.gundobin@mail.com"));
    }

    @Test
    void addUpdateDeletePerson_shouldModifyList() {
        // add a new person via dto
        var dto = new com.safetynet.alerts.dto.PersonDto("New", "Person", "1 Main St", "Nowhere", "00000", "000-000-0000", "new@mail.com");
        alertService.addPerson(dto);
        assertTrue(persons.stream().anyMatch(p -> "New".equals(p.getFirstName()) && "Person".equals(p.getLastName())));

        // update existing person
        var upd = new com.safetynet.alerts.dto.PersonDto("New", "Person", "2 Main St", "Nowhere", "11111", "111-111-1111", "new2@mail.com");
        boolean updated = alertService.updatePerson(upd);
        assertTrue(updated);
        Person p = persons.stream().filter(x -> "New".equals(x.getFirstName()) && "Person".equals(x.getLastName())).findFirst().orElse(null);
        assertNotNull(p);
        assertEquals("2 Main St", p.getAddress());

        // delete
        boolean deleted = alertService.deletePerson("New", "Person");
        assertTrue(deleted);
        assertFalse(persons.stream().anyMatch(x -> "New".equals(x.getFirstName()) && "Person".equals(x.getLastName())));
    }

    @Test
    void addUpdateDeleteFirestation_shouldModifyList() {
        var dto = new com.safetynet.alerts.dto.FirestationDto("1 Broadway", "5");
        alertService.addFirestation(dto);
        assertTrue(firestations.stream().anyMatch(f -> "1 Broadway".equals(f.getAddress()) && "5".equals(f.getStation())));

        var upd = new com.safetynet.alerts.dto.FirestationDto("1 Broadway", "9");
        boolean updated = alertService.updateFirestation(upd);
        assertTrue(updated);
        assertTrue(firestations.stream().anyMatch(f -> "1 Broadway".equals(f.getAddress()) && "9".equals(f.getStation())));

        boolean deleted = alertService.deleteFirestation("1 Broadway", null);
        assertTrue(deleted);
        assertFalse(firestations.stream().anyMatch(f -> "1 Broadway".equals(f.getAddress())));
    }

    @Test
    void addUpdateDeleteMedicalRecord_shouldModifyList() {
        var dto = new com.safetynet.alerts.dto.ResidentInfoDto("Mary", "Ann", "12/12/1990", List.of("med1"), List.of("all1"));
        alertService.addMedicalRecord(dto);
        assertTrue(medicalRecords.stream().anyMatch(m -> "Mary".equals(m.getFirstName()) && "Ann".equals(m.getLastName())));

        var updateDto = new com.safetynet.alerts.dto.ResidentInfoDto("Mary", "Ann", "01/01/1991", List.of("med2"), List.of("all2"));
        boolean updated = alertService.updateMedicalRecord(updateDto);
        assertTrue(updated);
        MedicalRecord m = medicalRecords.stream().filter(x -> "Mary".equals(x.getFirstName()) && "Ann".equals(x.getLastName())).findFirst().orElse(null);
        assertNotNull(m);
        assertEquals("01/01/1991", m.getBirthdate());

        boolean deleted = alertService.deleteMedicalRecord("Mary", "Ann");
        assertTrue(deleted);
        assertFalse(medicalRecords.stream().anyMatch(x -> "Mary".equals(x.getFirstName()) && "Ann".equals(x.getLastName())));
    }
}
