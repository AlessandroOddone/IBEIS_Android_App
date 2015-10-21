package edu.uic.ibeis_tourist.activity_enums;

/**
 * Request codes for startActivityForResult invocations
 */
public enum ActivityForResultRequestEnum {
    PICTURE_REQUEST(1001);

    private final int value;

    ActivityForResultRequestEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
