package edu.uic.ibeis_tourist.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import edu.uic.ibeis_tourist.values.PositionEvent;

public class PositionService extends Service {

    // Location provider
    private static final String PROVIDER = LocationManager./*GPS_PROVIDER;*/NETWORK_PROVIDER;
    // Minimum time interval between GPS updates in milliseconds
    private static final int MIN_TIME_UPDATES = 0;
    // Minimum distance interval between GPS updates in meters
    private static final float MIN_DIST_UPDATES = 0.1f;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    // latitude of the latest available position
    private double latitude;
    // longitude of the latest available position
    private double longitude;
    // azimuth in degrees from the latest available sensor data
    private float azimuth;

    // accelerometer and geomagnetic field sensor data from the latest sensor events
    private float[] accelerometerValues;
    private float[] geomagneticValues;


    // Override Service methods
    @Override
    public void onCreate() {
        System.out.println("GPS service created");
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new GPSLocationListener();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorEventListener = new GeomagneticSensorListener();

        if (locationManager.isProviderEnabled(PROVIDER)) {
            broadcastEvent(PositionEvent.GPS_ENABLED);
        }
        else {
            broadcastEvent(PositionEvent.GPS_DISABLED);
        }
        locationManager.requestLocationUpdates(PROVIDER, MIN_TIME_UPDATES, MIN_DIST_UPDATES, locationListener);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("GPS service started");
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Binding not supported
        throw new UnsupportedOperationException("Binding not supported");
    }

    @Override
    public void onDestroy() {
        System.out.println("GPS service destroyed");
        locationManager.removeUpdates(locationListener);
        super.onDestroy();
    }

    /**
     * Send broadcast message of an asynchronous event occurrence
     * If the event is a location change, send new latitude and longitude
     * @param e value from GpsEvent enum
     */
    private void broadcastEvent(PositionEvent e) {
        Intent intent = new Intent();
        intent.setAction("edu.uic.ibeis_tourist.broadcast_position_event");
        intent.putExtra("positionEvent", e.getValue());
        if (e == PositionEvent.LOCATION_CHANGED) {
            intent.putExtra("lat", latitude);
            intent.putExtra("lon", longitude);
        }
        if (e == PositionEvent.SENSOR_CHANGED) {
            intent.putExtra("azimuth", azimuth);
        }
        sendBroadcast(intent);
    }

    public class GPSLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            broadcastEvent(PositionEvent.LOCATION_CHANGED);

            System.out.println("New Location: (" + latitude + ", " + longitude + ")");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO implement
        }

        @Override
        public void onProviderEnabled(String provider) {
            System.out.println("Location Listener: GPS enabled");
            broadcastEvent(PositionEvent.GPS_ENABLED);
        }

        @Override
        public void onProviderDisabled(String provider) {
            System.out.println("Location Listener: GPS disabled");
            broadcastEvent(PositionEvent.GPS_DISABLED);
        }
    }

    public class GeomagneticSensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                accelerometerValues = event.values;

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                geomagneticValues = event.values;

            if (accelerometerValues != null && geomagneticValues != null) {
                float R[] = new float[9];

                if (SensorManager.getRotationMatrix(R, null, accelerometerValues, geomagneticValues)) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);

                    // orientation[0] is azimuth in radians, convert to degrees
                    azimuth = (float) Math.toDegrees(orientation[0]);
                    //System.out.println("Sensor Event Listener: Azimuth = " + azimuth);
                    broadcastEvent(PositionEvent.SENSOR_CHANGED);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
