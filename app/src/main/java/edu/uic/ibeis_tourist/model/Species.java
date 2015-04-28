package edu.uic.ibeis_tourist.model;

import edu.uic.ibeis_tourist.exceptions.InvalidSpeciesException;

public enum Species {
    GIRAFFE("Giraffe"), GREVY_ZEBRA("Grevy Zebra"), PLAIN_ZEBRA("Plain Zebra"), UNKNOWN("N/A");

    private String value;

    Species(String value) {
        this.value = value;
    }

    public String asString() {
        return value;
    }

    public static Species fromString(String value) throws InvalidSpeciesException{

        for(Species s : Species.values()) {
            if(s.asString().equals(value)) {
                return s;
            }
        }
        throw new InvalidSpeciesException();
    }
}
