package edu.uic.ibeis_tourist.values;

/**
 * GpsService events
 */
public enum PositionEvent {
    GPS_ENABLED(1001), GPS_DISABLED(1002), LOCATION_CHANGED(1003), SENSOR_CHANGED(1004);

    private final int value;

    private PositionEvent(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}