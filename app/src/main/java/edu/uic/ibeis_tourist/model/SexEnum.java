package edu.uic.ibeis_tourist.model;

import edu.uic.ibeis_tourist.exceptions.InvalidSexException;

public enum SexEnum {
    MALE("M"), FEMALE("F"), UNKNOWN("N/A");

    private String value;

    SexEnum(String value) {
        this.value = value;
    }

    public String asString() {
        return value;
    }

    public static SexEnum fromString(String value) throws InvalidSexException {

        for(SexEnum s : SexEnum.values()) {
            if(s.asString().equals(value)) {
                return s;
            }
        }
        throw new InvalidSexException();
    }
}
