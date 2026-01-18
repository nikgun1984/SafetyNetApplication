package com.safetynet.alerts.dto;

import java.util.List;

public class ChildInfoDto extends ResidentInfoDto {
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
        super();
        setFirstName(f);
        setLastName(l);
        setAge(age);
        this.householdMembers = members;
    }
}
