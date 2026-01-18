// java
package com.safetynet.alerts.controller;

import com.safetynet.alerts.dto.ChildInfoDto;
import com.safetynet.alerts.dto.FireAddressResponseDto;
import com.safetynet.alerts.dto.PersonInfoDto;
import com.safetynet.alerts.dto.ResidentInfoDto;
import com.safetynet.alerts.service.AlertService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        PersonInfoDto dto = new PersonInfoDto("Nick", "Gundobin", "1509 Culver St", "841-874-6512");
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
                .andExpect(jsonPath("$.persons[0].phone", is("841-874-6512")))
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
        List<String> phones = Arrays.asList("841-874-6512", "841-874-6513");

        when(alertService.getPhoneAlert("2")).thenReturn(phones);

        mockMvc.perform(get("/phoneAlert").param("firestation", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", is("841-874-6512")))
                .andExpect(jsonPath("$[1]", is("841-874-6513")));
    }

    @Test
    void getFire_shouldReturnStationAndResidentsForAddress() throws Exception {
        // phone and age must be in the correct positions
        ResidentInfoDto resident = new ResidentInfoDto("John", "Doe", "841-874-6512", 23);
        FireAddressResponseDto fireDto = new FireAddressResponseDto("3", Arrays.asList(resident));

        doReturn(fireDto).when(alertService).getFire("1509 Culver St");

        mockMvc.perform(get("/fire").param("address", "1509 Culver St"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.station", is("3")))
                .andExpect(jsonPath("$.residents", hasSize(1)))
                .andExpect(jsonPath("$.residents[0].firstName", is("John")))
                .andExpect(jsonPath("$.residents[0].lastName", is("Doe")))
                .andExpect(jsonPath("$.residents[0].phone", is("841-874-6512")));
    }

    @Test
    void getFloodStations_shouldReturnHouseholdsGroupedByAddress() throws Exception {
        // prepare test data
        ResidentInfoDto resident = new ResidentInfoDto("John", "Doe", "841-874-6512", 23);
        Map<String, List<ResidentInfoDto>> resp = new HashMap<>();
        resp.put("1509 Culver St", Arrays.asList(resident));

        // service is expected to be called with the parsed list ["1","2"]
        when(alertService.getFloodStations(Arrays.asList("1", "2"))).thenReturn(resp);

        mockMvc.perform(get("/flood/stations").param("stations", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['1509 Culver St']", hasSize(1)))
                .andExpect(jsonPath("$['1509 Culver St'][0].firstName", is("John")))
                .andExpect(jsonPath("$['1509 Culver St'][0].lastName", is("Doe")))
                .andExpect(jsonPath("$['1509 Culver St'][0].phone", is("841-874-6512")))
                .andExpect(jsonPath("$['1509 Culver St'][0].age", is(23)));
    }
}
