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

/**
 * Service responsible for managing the persistence and in-memory storage of application data.
 * <p>
 * This service handles:
 * <ul>
 *   <li>Loading data from a JSON file on application startup</li>
 *   <li>Maintaining in-memory collections of persons, fire stations, and medical records</li>
 *   <li>Persisting data changes back to the JSON file</li>
 * </ul>
 * </p>
 * <p>
 * The data is stored in {@code data/data.json} and is automatically loaded during the
 * {@link PostConstruct} phase. All CRUD operations trigger automatic persistence to ensure
 * data consistency between restarts.
 * </p>
 * 
 * @see Person
 * @see Firestation
 * @see MedicalRecord
 */
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
