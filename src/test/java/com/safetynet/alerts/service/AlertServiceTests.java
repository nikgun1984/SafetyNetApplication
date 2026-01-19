package com.safetynet.alerts.service;

import com.safetynet.alerts.model.Firestation;
import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

        persons.add(new Person("John", "Doe", "1509 Highland Oaks Dr", "Tampa", "34638", "305-874-6512", "john.doe@mail.com"));
        persons.add(new Person("Jane", "Doe", "1509 Highland Oaks Dr", "Tampa", "34638", "305-874-6513", "jane.doe@mail.com"));
        persons.add(new Person("Kid", "Young", "1509 Highland Oaks Dr", "Tampa", "34638", "305-874-6514", "kid.young@mail.com"));

        medicalRecords.add(new MedicalRecord("John", "Doe", "03/06/1984", List.of("med1"), List.of("nuts")));
        medicalRecords.add(new MedicalRecord("Jane", "Doe", "01/01/1980", List.of(), List.of()));
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
}
