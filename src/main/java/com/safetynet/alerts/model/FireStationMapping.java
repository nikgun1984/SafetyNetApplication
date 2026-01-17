package com.safetynet.alerts.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FireStationMapping {
    public String address;
    public String station;
}
