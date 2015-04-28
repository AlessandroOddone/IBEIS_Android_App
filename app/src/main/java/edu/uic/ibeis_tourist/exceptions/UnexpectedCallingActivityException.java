package edu.uic.ibeis_tourist.exceptions;

public class UnexpectedCallingActivityException extends Exception {

    public UnexpectedCallingActivityException() {
        super();
    }

    public UnexpectedCallingActivityException(String detailMessage) {
        super(detailMessage);
    }

    public UnexpectedCallingActivityException(Throwable throwable) {
        super(throwable);
    }

    public UnexpectedCallingActivityException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}