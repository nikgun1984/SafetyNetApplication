package com.safetynet.alerts.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.model.DataWrapper;
import com.safetynet.alerts.model.Person;
import com.safetynet.alerts.model.Firestation;
import com.safetynet.alerts.model.MedicalRecord;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Getter
@Service
public class DataService {
    private List<Person> persons = Collections.emptyList();
    private List<Firestation> firestations = Collections.emptyList();
    private List<MedicalRecord> medicalrecords = Collections.emptyList();

    @PostConstruct
    public void loadData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = new ClassPathResource("data.json").getInputStream();
            DataWrapper wrapper = mapper.readValue(is, DataWrapper.class);
            this.persons = wrapper.getPersons() != null ? wrapper.getPersons() : Collections.emptyList();
            this.firestations = wrapper.getFirestations() != null ? wrapper.getFirestations() : Collections.emptyList();
            this.medicalrecords = wrapper.getMedicalrecords() != null ? wrapper.getMedicalrecords() : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load data.json", e);
        }
    }

}
