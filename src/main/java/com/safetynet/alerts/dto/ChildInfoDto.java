package com.safetynet.alerts.dto;

import java.util.List;

public class ChildInfoDto {
    public String firstName;
    public String lastName;
    public int age;
    public List<HouseholdMember> householdMembers;

    public static class HouseholdMember {
        public String firstName;
        public String lastName;

        public HouseholdMember(String f, String l) {
            this.firstName = f;
            this.lastName = l;
        }
    }

    public ChildInfoDto(String f, String l, int age, List<HouseholdMember> members) {
        this.firstName = f;
        this.lastName = l;
        this.age = age;
        this.householdMembers = members;
    }
}
