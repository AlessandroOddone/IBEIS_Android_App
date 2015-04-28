package edu.uic.ibeis_tourist.exceptions;

public class InvalidSexException extends Exception {

    public InvalidSexException() {
        super();
    }

    public InvalidSexException(String detailMessage) {
        super(detailMessage);
    }

    public InvalidSexException(Throwable throwable) {
        super(throwable);
    }

    public InvalidSexException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}