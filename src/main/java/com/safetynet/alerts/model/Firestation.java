package com.safetynet.alerts.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Setter;

@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Firestation {
    public String address;
    public String station;
}
