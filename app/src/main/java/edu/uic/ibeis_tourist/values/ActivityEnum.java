package edu.uic.ibeis_tourist.values;

public enum ActivityEnum {

    MainActivity(1001), MyPicturesActivity(1002), MyPictureDetailActivity(1003);

    private int value;

    ActivityEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
