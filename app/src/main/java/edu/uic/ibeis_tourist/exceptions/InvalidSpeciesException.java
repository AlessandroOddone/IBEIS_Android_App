package edu.uic.ibeis_tourist.exceptions;

public class InvalidSpeciesException extends Exception {

    public InvalidSpeciesException() {
        super();
    }

    public InvalidSpeciesException(String detailMessage) {
        super(detailMessage);
    }

    public InvalidSpeciesException(Throwable throwable) {
        super(throwable);
    }

    public InvalidSpeciesException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}