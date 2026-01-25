package com.safetynet.alerts.service;

import com.safetynet.alerts.dto.PersonDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DataPersistenceTest {

    @Autowired
    private DataService dataService;

    @Autowired
    private AlertService alertService;

    @Test
    void testDataPersistence() throws Exception {
        // Get initial size
        int initialPersonCount = dataService.getPersons().size();
        
        // Add a new person
        PersonDto newPerson = new PersonDto(
                "TestFirstName", 
                "TestLastName", 
                "123 Test St", 
                "TestCity", 
                "12345", 
                "555-1234", 
                "test@test.com"
        );
        alertService.addPerson(newPerson);
        
        // Verify person was added to in-memory list
        assertEquals(initialPersonCount + 1, dataService.getPersons().size());
        
        // Verify the file was updated
        File dataFile = new File("data/data.json");
        assertTrue(dataFile.exists(), "Data file should exist");
        
        String fileContent = Files.readString(dataFile.toPath());
        assertTrue(fileContent.contains("TestFirstName"), "File should contain the new person");
        assertTrue(fileContent.contains("TestLastName"), "File should contain the new person");
        
        // Clean up - delete the test person
        alertService.deletePerson("TestFirstName", "TestLastName");
        
        // Verify person was removed
        assertEquals(initialPersonCount, dataService.getPersons().size());
        
        // Verify the file was updated again
        String updatedContent = Files.readString(dataFile.toPath());
        assertFalse(updatedContent.contains("TestFirstName"), "File should not contain the deleted person");
    }
}
