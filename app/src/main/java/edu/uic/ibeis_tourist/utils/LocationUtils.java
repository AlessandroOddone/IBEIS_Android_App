package edu.uic.ibeis_tourist.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import edu.uic.ibeis_tourist.model.Location;

public class LocationUtils {

    public static boolean isPositionAtLocation(double latitude, double longitude, Location location) {
        LatLng position = new LatLng(latitude, longitude);

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(new LatLng(location.getBounds().getSouthwestBound().getLatitude(),
                        location.getBounds().getSouthwestBound().getLongitude()))
                .include(new LatLng(location.getBounds().getNortheastBound().getLatitude(),
                        location.getBounds().getNortheastBound().getLongitude())).build();

        return bounds.contains(position);
    }
}
