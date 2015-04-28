package edu.uic.ibeis_tourist.model;

import edu.uic.ibeis_tourist.exceptions.InvalidSexException;

public enum Sex {
    MALE("M"), FEMALE("F"), UNKNOWN("N/A");

    private String value;

    Sex(String value) {
        this.value = value;
    }

    public String asString() {
        return value;
    }

    public static Sex fromString(String value) throws InvalidSexException {

        for(Sex s : Sex.values()) {
            if(s.asString().equals(value)) {
                return s;
            }
        }
        throw new InvalidSexException();
    }
}
