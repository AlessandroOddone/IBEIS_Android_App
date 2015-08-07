package edu.uic.ibeis_tourist.model;

import edu.uic.ibeis_tourist.exceptions.InvalidSpeciesException;

public enum SpeciesEnum {
    GIRAFFE("Giraffe"), UNKNOWN("N/A");

    private String value;

    SpeciesEnum(String value) {
        this.value = value;
    }

    public String asString() {
        return value;
    }

    public static SpeciesEnum fromString(String value) throws InvalidSpeciesException{

        for(SpeciesEnum s : SpeciesEnum.values()) {
            if(s.asString().equals(value)) {
                return s;
            }
        }
        throw new InvalidSpeciesException();
    }
}
