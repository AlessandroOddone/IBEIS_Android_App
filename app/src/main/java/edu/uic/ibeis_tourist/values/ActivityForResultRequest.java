package edu.uic.ibeis_tourist.values;

/**
 * Request codes for startActivityForResult invocations
 */
public enum ActivityForResultRequest {
    PICTURE_REQUEST(1001);

    private final int value;

    private ActivityForResultRequest(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
