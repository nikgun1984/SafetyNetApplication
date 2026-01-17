package com.safetynet.alerts.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Person {
    public String firstName;
    public String lastName;
    public String address;
    public String city;
    public String zip;
    public String phone;
    public String email;
}
