package edu.uic.ibeis_tourist.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Position implements Parcelable {

    // latitude
    private double latitude;
    // longitude
    private double longitude;
    // direction faced by the phone in degrees (NORTH:0, EAST:90, SOUTH:180, WEST:270)
    private float facingDirection;

    public Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Position(double latitude, double longitude, float facingDirection) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.facingDirection = facingDirection;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getFacingDirection() {
        return facingDirection;
    }


    //Implementation of Parcelable interface
    public Position(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        facingDirection = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeFloat(facingDirection);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Position createFromParcel(Parcel in) {
            return new Position(in);
        }
        public Position[] newArray(int size) {
            return new Position[size];
        }
    };
}
