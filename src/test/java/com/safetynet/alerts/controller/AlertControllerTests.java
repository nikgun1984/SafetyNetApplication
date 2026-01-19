package com.safetynet.alerts.controller;

import com.safetynet.alerts.dto.ChildInfoDto;
import com.safetynet.alerts.dto.FireAddressResponseDto;
import com.safetynet.alerts.dto.PersonDto;
import com.safetynet.alerts.dto.ResidentInfoDto;
import com.safetynet.alerts.service.AlertService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertController.class)
@Import(AlertControllerTests.TestConfig.class)
class AlertControllerTests {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AlertService alertService() {
            return Mockito.mock(AlertService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlertService alertService;

    @Test
    void getFirestation_shouldReturnPersonsAndCounts() throws Exception {
        ResidentInfoDto dto = new ResidentInfoDto("Nick", "Gundobin", "1509 Culver St", "305-510-9943");
        Map<String, Object> resp = new HashMap<>();
        resp.put("persons", Arrays.asList(dto));
        resp.put("children", 1);
        resp.put("adults", 2);

        when(alertService.getFirestationPeople("1")).thenReturn(resp);

        mockMvc.perform(get("/firestation").param("stationNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.persons", hasSize(1)))
                .andExpect(jsonPath("$.persons[0].firstName", is("Nick")))
                .andExpect(jsonPath("$.persons[0].lastName", is("Gundobin")))
                .andExpect(jsonPath("$.persons[0].address", is("1509 Culver St")))
                .andExpect(jsonPath("$.persons[0].phone", is("305-510-9943")))
                .andExpect(jsonPath("$.children", is(1)))
                .andExpect(jsonPath("$.adults", is(2)));
    }

    @Test
    void getChildAlert_shouldReturnChildrenWithHouseholdMembers() throws Exception {
        ChildInfoDto.HouseholdMember hm = new ChildInfoDto.HouseholdMember("Nick", "Gundobin");
        ChildInfoDto child = new ChildInfoDto("Jane", "Gundobin", 8, Arrays.asList(hm));

        when(alertService.getChildAlert("1509 Culver St")).thenReturn(Arrays.asList(child));

        mockMvc.perform(get("/childAlert").param("address", "1509 Culver St"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is("Jane")))
                .andExpect(jsonPath("$[0].lastName", is("Gundobin")))
                .andExpect(jsonPath("$[0].age", is(8)))
                .andExpect(jsonPath("$[0].householdMembers", hasSize(1)))
                .andExpect(jsonPath("$[0].householdMembers[0].firstName", is("Nick")))
                .andExpect(jsonPath("$[0].householdMembers[0].lastName", is("Gundobin")));
    }

    @Test
    void getPhoneAlert_shouldReturnPhoneListForStation() throws Exception {
        List<String> phones = Arrays.asList("305-510-9943", "305-510-9944");

        when(alertService.getPhoneAlert("2")).thenReturn(phones);

        mockMvc.perform(get("/phoneAlert").param("firestation", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", is("305-510-9943")))
                .andExpect(jsonPath("$[1]", is("305-510-9944")));
    }

    @Test
    void getFire_shouldReturnStationAndResidentsForAddress() throws Exception {
        ResidentInfoDto resident = new ResidentInfoDto("John", "Doe", "305-510-9943", 23);
        FireAddressResponseDto fireDto = new FireAddressResponseDto("3", Arrays.asList(resident));

        doReturn(fireDto).when(alertService).getFire("1509 Culver St");

        mockMvc.perform(get("/fire").param("address", "1509 Culver St"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.station", is("3")))
                .andExpect(jsonPath("$.residents", hasSize(1)))
                .andExpect(jsonPath("$.residents[0].firstName", is("John")))
                .andExpect(jsonPath("$.residents[0].lastName", is("Doe")))
                .andExpect(jsonPath("$.residents[0].phone", is("305-510-9943")));
    }

    @Test
    void getFloodStations_shouldReturnHouseholdsGroupedByAddress() throws Exception {
        ResidentInfoDto resident = new ResidentInfoDto("John", "Doe", "305-510-9943", 23);
        Map<String, List<ResidentInfoDto>> resp = new HashMap<>();
        resp.put("1509 Culver St", Arrays.asList(resident));

        when(alertService.getFloodStations(Arrays.asList("1", "2"))).thenReturn(resp);

        mockMvc.perform(get("/flood/stations").param("stations", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['1509 Culver St']", hasSize(1)))
                .andExpect(jsonPath("$['1509 Culver St'][0].firstName", is("John")))
                .andExpect(jsonPath("$['1509 Culver St'][0].lastName", is("Doe")))
                .andExpect(jsonPath("$['1509 Culver St'][0].phone", is("305-510-9943")))
                .andExpect(jsonPath("$['1509 Culver St'][0].age", is(23)));
    }

    @Test
    void getPersonInfo_shouldReturnResidentsWithMedicalHistory() throws Exception {
        ResidentInfoDto dto = new ResidentInfoDto(
                "John",
                "Doe",
                "1509 Culver St",
                34,
                "john.doe@example.com",
                Arrays.asList("aspirin:100mg"),
                Arrays.asList("peanuts")
        );

        when(alertService.getPersonInfoByLastName("Doe")).thenReturn(Arrays.asList(dto));

        mockMvc.perform(get("/personInfo").param("lastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[0].lastName", is("Doe")))
                .andExpect(jsonPath("$[0].address", is("1509 Culver St")))
                .andExpect(jsonPath("$[0].age", is(34)))
                .andExpect(jsonPath("$[0].email", is("john.doe@example.com")))
                .andExpect(jsonPath("$[0].medications", hasSize(1)))
                .andExpect(jsonPath("$[0].medications[0]", is("aspirin:100mg")))
                .andExpect(jsonPath("$[0].allergies", hasSize(1)))
                .andExpect(jsonPath("$[0].allergies[0]", is("peanuts")));
    }

    @Test
    void getCommunityEmail_shouldReturnEmailsForCity() throws Exception {
        List<String> emails = Arrays.asList("john.doe@example.com", "jane.doe@example.com");

        when(alertService.getEmailsByCity("Culver")).thenReturn(emails);

        mockMvc.perform(get("/communityEmail").param("city", "Culver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", is("john.doe@example.com")))
                .andExpect(jsonPath("$[1]", is("jane.doe@example.com")));
    }

    @Test
    void addFirestation_shouldReturnCreatedAndLocationHeader() throws Exception {
        String json = "{"
                + "\"address\":\"1509 Culver St\","
                + "\"station\":\"1\""
                + "}";

        doNothing().when(alertService).addFirestation(any());

        mockMvc.perform(post("/firestation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/firestation?address=1509%20Culver%20St")));
    }

    @Test
    void addFirestation_whenMissingFields_shouldReturnBadRequest() throws Exception {
        String json = "{"
                + "\"address\":\"1509 Culver St\""
                + "}";

        mockMvc.perform(post("/firestation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFirestation_whenExists_shouldReturnOk() throws Exception {
        String json = "{"
                + "\"address\":\"1509 Culver St\","
                + "\"station\":\"2\""
                + "}";

        when(alertService.updateFirestation(any())).thenReturn(true);

        mockMvc.perform(put("/firestation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void updateFirestation_whenNotFound_shouldReturnNotFound() throws Exception {
        String json = "{"
                + "\"address\":\"Nowhere\","
                + "\"station\":\"99\""
                + "}";

        when(alertService.updateFirestation(any())).thenReturn(false);

        mockMvc.perform(put("/firestation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteFirestation_byStation_shouldReturnOk() throws Exception {
        String json = "{ \"station\":\"2\" }";

        when(alertService.deleteFirestation(null, "2")).thenReturn(true);

        mockMvc.perform(delete("/firestation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void deleteFirestation_byAddress_shouldReturnOk() throws Exception {
        String json = "{ \"address\":\"1509 Culver St\" }";

        when(alertService.deleteFirestation("1509 Culver St", null)).thenReturn(true);

        mockMvc.perform(delete("/firestation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void deleteFirestation_whenNoBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/firestation"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteFirestation_whenNotFound_shouldReturnNotFound() throws Exception {
        String json = "{ \"station\":\"42\" }";

        when(alertService.deleteFirestation(null, "42")).thenReturn(false);

        mockMvc.perform(delete("/firestation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void addPerson_shouldReturnCreatedAndLocationHeader() throws Exception {
        String json = "{"
                + "\"firstName\":\"John\","
                + "\"lastName\":\"Doe\","
                + "\"address\":\"1509 Culver St\","
                + "\"city\":\"Culver\","
                + "\"zip\":\"97451\","
                + "\"phone\":\"305-510-9943\","
                + "\"email\":\"john.doe@example.com\""
                + "}";

        doNothing().when(alertService).addPerson(any(PersonDto.class));

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/person?firstName=John&lastName=Doe")));
    }

    @Test
    void addPerson_whenNoBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/person"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePerson_whenExists_shouldReturnOk() throws Exception {
        String json = "{"
                + "\"firstName\":\"John\","
                + "\"lastName\":\"Doe\","
                + "\"address\":\"1509 Culver St\","
                + "\"city\":\"Culver\","
                + "\"zip\":\"97451\","
                + "\"phone\":\"305-510-9943\","
                + "\"email\":\"john.doe@example.com\""
                + "}";

        when(alertService.updatePerson(any(PersonDto.class))).thenReturn(true);

        mockMvc.perform(put("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void updatePerson_whenNotFound_shouldReturnNotFound() throws Exception {
        String json = "{"
                + "\"firstName\":\"Non\","
                + "\"lastName\":\"Existent\","
                + "\"address\":\"Nowhere\""
                + "}";

        when(alertService.updatePerson(any(PersonDto.class))).thenReturn(false);

        mockMvc.perform(put("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePerson_byBody_whenExists_shouldReturnOk() throws Exception {
        String json = "{ \"firstName\":\"Non\", \"lastName\":\"Existent\" }";

        when(alertService.deletePerson("Non", "Existent")).thenReturn(true);

        mockMvc.perform(delete("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void deletePerson_whenMissingFields_shouldReturnBadRequest() throws Exception {
        // missing lastName -> controller should return 400
        String json = "{ \"firstName\":\"Only\" }";

        mockMvc.perform(delete("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletePerson_whenNotFound_shouldReturnNotFound() throws Exception {
        String json = "{ \"firstName\":\"Non\", \"lastName\":\"Existent\" }";

        when(alertService.deletePerson("Non", "Existent")).thenReturn(false);

        mockMvc.perform(delete("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void addMedicalRecord_shouldReturnCreatedAndLocationHeader() throws Exception {
        String json = "{"
                + "\"firstName\":\"John\","
                + "\"lastName\":\"Doe\","
                + "\"birthdate\":\"01/01/1990\","
                + "\"medications\":[\"aspirin:100mg\"],"
                + "\"allergies\":[\"peanuts\"]"
                + "}";

        doNothing().when(alertService).addMedicalRecord(any(ResidentInfoDto.class));

        mockMvc.perform(post("/medicalRecord")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/medicalRecord?firstName=John&lastName=Doe")));
    }

    @Test
    void addMedicalRecord_whenNoBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/medicalRecord"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMedicalRecord_whenExists_shouldReturnOk() throws Exception {
        String json = "{"
                + "\"firstName\":\"John\","
                + "\"lastName\":\"Doe\","
                + "\"birthdate\":\"01/01/1990\","
                + "\"medications\":[\"aspirin:100mg\"],"
                + "\"allergies\":[\"peanuts\"]"
                + "}";

        when(alertService.updateMedicalRecord(any(ResidentInfoDto.class))).thenReturn(true);

        mockMvc.perform(put("/medicalRecord")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void updateMedicalRecord_whenNotFound_shouldReturnNotFound() throws Exception {
        String json = "{"
                + "\"firstName\":\"Non\","
                + "\"lastName\":\"Existent\","
                + "\"birthdate\":\"01/01/1990\""
                + "}";

        when(alertService.updateMedicalRecord(any(ResidentInfoDto.class))).thenReturn(false);

        mockMvc.perform(put("/medicalRecord")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMedicalRecord_byBody_whenExists_shouldReturnOk() throws Exception {
        String json = "{ \"firstName\":\"John\", \"lastName\":\"Doe\" }";

        when(alertService.deleteMedicalRecord("John", "Doe")).thenReturn(true);

        mockMvc.perform(delete("/medicalRecord")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void deleteMedicalRecord_whenMissingFields_shouldReturnBadRequest() throws Exception {
        // missing lastName -> controller should return 400
        String json = "{ \"firstName\":\"Only\" }";

        mockMvc.perform(delete("/medicalRecord")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteMedicalRecord_whenNotFound_shouldReturnNotFound() throws Exception {
        String json = "{ \"firstName\":\"Non\", \"lastName\":\"Existent\" }";

        when(alertService.deleteMedicalRecord("Non", "Existent")).thenReturn(false);

        mockMvc.perform(delete("/medicalRecord")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }
}
