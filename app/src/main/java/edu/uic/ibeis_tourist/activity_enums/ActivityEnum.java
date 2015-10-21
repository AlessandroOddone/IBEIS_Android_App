package edu.uic.ibeis_tourist.activity_enums;

public enum ActivityEnum {

    MainActivity(1001), MyPicturesActivity(1002), AnnotatePictureActivity(1003), PictureDetailActivity(1004);

    private int value;

    ActivityEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
