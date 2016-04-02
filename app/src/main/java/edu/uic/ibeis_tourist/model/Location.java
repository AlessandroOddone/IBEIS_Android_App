package edu.uic.ibeis_tourist.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Location implements Parcelable {

    private int id;
    private String name;
    private LocationBounds bounds;

    public Location() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocationBounds getBounds() {
        return bounds;
    }

    public void setBounds(LocationBounds bounds) {
        this.bounds = bounds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Implementation of Parcelable interface
    public Location(Parcel in){
        id = in.readInt();
        name = in.readString();
        bounds = in.readParcelable(LocationBounds.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeParcelable(bounds, flags);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };
}
