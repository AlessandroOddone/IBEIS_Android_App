package edu.uic.ibeis_tourist.model;

import android.os.Parcel;
import android.os.Parcelable;

// defines the bounds of a location on a map (rectangular shape defined by two points)
public class LocationBounds implements Parcelable {
    Position southwestBound;
    Position northeastBound;

    public LocationBounds(Position southwestBound, Position northwestBound) {
        this.southwestBound = southwestBound;
        this.northeastBound = northwestBound;
    }

    public Position getSouthwestBound() {
        return southwestBound;
    }

    public Position getNortheastBound() {
        return northeastBound;
    }

    //Implementation of Parcelable interface
    public LocationBounds(Parcel in) {
        southwestBound = in.readParcelable(Position.class.getClassLoader());
        northeastBound = in.readParcelable(Position.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(southwestBound, flags);
        dest.writeParcelable(northeastBound, flags);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public LocationBounds createFromParcel(Parcel in) {
            return new LocationBounds(in);
        }
        public LocationBounds[] newArray(int size) {
            return new LocationBounds[size];
        }
    };
}
