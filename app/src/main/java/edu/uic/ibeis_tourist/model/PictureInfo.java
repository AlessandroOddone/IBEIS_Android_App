package edu.uic.ibeis_tourist.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.GregorianCalendar;

import edu.uic.ibeis_tourist.exceptions.InvalidSexException;
import edu.uic.ibeis_tourist.exceptions.InvalidSpeciesException;

public class PictureInfo implements Parcelable {
    private String fileName;
    private GregorianCalendar dateTime;
    private Position position;
    private String individualName;
    private Species individualSpecies;
    private Sex individualSex;
    private Location location;

    public PictureInfo() {}

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public GregorianCalendar getDateTime() {
        return dateTime;
    }

    public void setDateTime(GregorianCalendar dateTime) {
        this.dateTime = dateTime;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getIndividualName() {
        return individualName;
    }

    public void setIndividualName(String individualName) {
        this.individualName = individualName;
    }

    public Species getIndividualSpecies() {
        return individualSpecies;
    }

    public void setIndividualSpecies(Species individualSpecies) {
        this.individualSpecies = individualSpecies;
    }

    public void setIndividualSpecies(String individualSpecies) throws InvalidSpeciesException {
        this.individualSpecies = Species.fromString(individualSpecies);
    }

    public Sex getIndividualSex() {
        return individualSex;
    }

    public void setIndividualSex(Sex individualSex) {
        this.individualSex = individualSex;
    }

    public void setIndividualSex(String individualSex) throws InvalidSexException {
        this.individualSex = Sex.fromString(individualSex);
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    // Implementation of Parcelable interface

    public PictureInfo(Parcel in){
        try {
            fileName = in.readString();
            dateTime = new GregorianCalendar();
            dateTime.setTimeInMillis(in.readLong());
            position = new Position(in.readDouble(), in.readDouble());
            individualName = in.readString();
            individualSex = Sex.fromString(in.readString());
            individualSpecies = Species.fromString(in.readString());
            location = in.readParcelable(Location.class.getClassLoader());
        } catch (InvalidSexException | InvalidSpeciesException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileName);
        dest.writeLong(dateTime.getTimeInMillis());
        dest.writeDouble(position.getLatitude());
        dest.writeDouble(position.getLongitude());
        dest.writeString(individualName);
        dest.writeString(individualSex.asString());
        dest.writeString(individualSpecies.asString());
        dest.writeParcelable(location, 0);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PictureInfo createFromParcel(Parcel in) {
            return new PictureInfo(in);
        }
        public PictureInfo[] newArray(int size) {
            return new PictureInfo[size];
        }
    };
}