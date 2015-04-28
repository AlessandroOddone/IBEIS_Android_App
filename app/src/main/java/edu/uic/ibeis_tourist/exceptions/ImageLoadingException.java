package edu.uic.ibeis_tourist.exceptions;

public class ImageLoadingException extends Exception {

    public ImageLoadingException() {
        super();
    }

    public ImageLoadingException(String detailMessage) {
        super(detailMessage);
    }

    public ImageLoadingException(Throwable throwable) {
        super(throwable);
    }

    public ImageLoadingException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}