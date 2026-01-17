package com.safetynet.alerts.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.model.DataWrapper;
import com.safetynet.alerts.model.Person;
import com.safetynet.alerts.model.FireStationMapping;
import com.safetynet.alerts.model.MedicalRecord;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Service
public class DataService {
    private List<Person> persons = Collections.emptyList();
    private List<FireStationMapping> firestations = Collections.emptyList();
    private List<MedicalRecord> medicalrecords = Collections.emptyList();

    @PostConstruct
    public void loadData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = new ClassPathResource("data.json").getInputStream();
            DataWrapper wrapper = mapper.readValue(is, DataWrapper.class);
            this.persons = wrapper.persons != null ? wrapper.persons : Collections.emptyList();
            this.firestations = wrapper.firestations != null ? wrapper.firestations : Collections.emptyList();
            this.medicalrecords = wrapper.medicalrecords != null ? wrapper.medicalrecords : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load data.json", e);
        }
    }

    public List<Person> getPersons() { return persons; }
    public List<FireStationMapping> getFirestations() { return firestations; }
    public List<MedicalRecord> getMedicalrecords() { return medicalrecords; }
}
