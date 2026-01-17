package com.safetynet.alerts.dto;

public class PersonInfoDto {
    public String firstName;
    public String lastName;
    public String address;
    public String phone;

    public PersonInfoDto(String f, String l, String a, String p) {
        this.firstName = f;
        this.lastName = l;
        this.address = a;
        this.phone = p;
    }
}
