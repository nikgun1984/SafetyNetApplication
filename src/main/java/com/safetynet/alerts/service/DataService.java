package com.safetynet.alerts.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.safetynet.alerts.model.DataWrapper;
import com.safetynet.alerts.model.Person;
import com.safetynet.alerts.model.Firestation;
import com.safetynet.alerts.model.MedicalRecord;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Service
public class DataService {
    private static final Logger log = LoggerFactory.getLogger(DataService.class);
    private static final String DATA_FILE_PATH = "data/data.json";
    
    private final ObjectMapper mapper;
    private final File dataFile;
    
    private List<Person> persons = new ArrayList<>();
    private List<Firestation> firestations = new ArrayList<>();
    private List<MedicalRecord> medicalrecords = new ArrayList<>();

    public DataService() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.dataFile = new File(DATA_FILE_PATH);
    }

    @PostConstruct
    public void loadData() {
        try {
            if (!dataFile.exists()) {
                log.warn("Data file not found at {}, initializing with empty data", DATA_FILE_PATH);
                this.persons = new ArrayList<>();
                this.firestations = new ArrayList<>();
                this.medicalrecords = new ArrayList<>();
                return;
            }
            
            DataWrapper wrapper = mapper.readValue(dataFile, DataWrapper.class);
            this.persons = wrapper.getPersons() != null ? new ArrayList<>(wrapper.getPersons()) : new ArrayList<>();
            this.firestations = wrapper.getFirestations() != null ? new ArrayList<>(wrapper.getFirestations()) : new ArrayList<>();
            this.medicalrecords = wrapper.getMedicalrecords() != null ? new ArrayList<>(wrapper.getMedicalrecords()) : new ArrayList<>();
            
            log.info("Loaded data from {}: {} persons, {} firestations, {} medical records", 
                    DATA_FILE_PATH, persons.size(), firestations.size(), medicalrecords.size());
        } catch (Exception e) {
            log.error("Failed to load data from {}", DATA_FILE_PATH, e);
            throw new RuntimeException("Failed to load data.json", e);
        }
    }

    public synchronized void saveData() {
        try {
            DataWrapper wrapper = new DataWrapper();
            wrapper.setPersons(persons);
            wrapper.setFirestations(firestations);
            wrapper.setMedicalrecords(medicalrecords);
            
            mapper.writeValue(dataFile, wrapper);
            log.debug("Data persisted to {}", DATA_FILE_PATH);
        } catch (IOException e) {
            log.error("Failed to save data to {}", DATA_FILE_PATH, e);
            throw new RuntimeException("Failed to persist data", e);
        }
    }
}
