package alpha.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;

public class LocationFinder implements LocationListener {
    public static long time, time_last;
    protected static LocationManager locationManager;
    protected static MainActivity activity;
    protected static URLBuilder urlBuilder;
    protected static Location location;
    protected static long updateInterval = 30; //seconds

    public LocationFinder(MainActivity activity, URLBuilder urlBuilder) {
        this.activity = activity;
        this.urlBuilder = urlBuilder;
        time_last = SystemClock.elapsedRealtime();  //to initiate the timer on object creation
    }

    public Location getLocation() {
        Location location;

        this.requestNewLocation();
        location = checkLastKnownLocation();

        if (location != null)
            return location;
        location = getLastStoredLocation();
        if (location != null)
            return location;
        return null;
    }

    /**
     * @return Returns the last known location after checking all providers or null if none is available
     * @throws SecurityException
     */
    protected Location checkLastKnownLocation() throws SecurityException {
        List<String> providers = locationManager.getProviders(false);
        for (String provider : providers) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                return location;
            }
        }
        return null;
    }

    /**
     * Stored locations are checked on write so they are always valid
     *
     * @return Return null when the location stored in the prefs is not usable (a location has not been written to the file yet). Only returns null on a new install
     */
    private Location getLastStoredLocation() {
        Location location = new Location("");
        Double lat, lon;
        boolean isUsable = true;

        SharedPreferences prefs = activity.getSharedPreferences("Coordinates", Context.MODE_PRIVATE);
        SharedPreferences.Editor e;

        isUsable = prefs.getBoolean("isUsable", false);

        if (isUsable) {
            lat = Double.longBitsToDouble(prefs.getLong("lat", 0));
            lon = Double.longBitsToDouble(prefs.getLong("lon", 0));
            location.setLatitude(lat);
            location.setLongitude(lon);
            return location;
        } else
            return null;
    }

    /**
     * Requests an update on the location which will arrive through onLocationChanged
     *
     * @return Returns last known location
     * @throws SecurityException on 6.0+ because it does not implement the support for runtime permissions
     */
    protected void requestNewLocation() throws SecurityException {

        locationManager = (LocationManager) activity.getSystemService(Context.
                LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
    }


    public void storeLocation(Location location) {
        //the API has been updated and coordinates now return a legal XML, no need to check which produced a SAX error
        SharedPreferences prefs = activity.getSharedPreferences("Coordinates", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("lat", Double.doubleToLongBits(location.getLatitude()));
        editor.putLong("lon", Double.doubleToLongBits(location.getLongitude()));
        editor.putBoolean("isUsable", true);
        editor.apply();
    }

    /**
     * Needs to be called before closing the app by the main activity
     *
     * @throws SecurityException
     */
    protected void removeUpdates() throws SecurityException {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        time = SystemClock.elapsedRealtime();
        if (time - time_last >= updateInterval * Math.pow(10, 3)) {
            urlBuilder.build(location);
            Log.d("@@@@@@@@@@@@@@@@@@", "onLocationChanged: " + location.getLatitude() + " " + location.getLongitude());
            time_last = SystemClock.elapsedRealtime(); //millis
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {
        LocationProvider provider = locationManager.getProvider(s);
        Log.d("@@@@@@@@@@@@@@@@@@", "onProviderEnabled: Location Provider " + provider + " has been enabled");
    }

    @Override
    public void onProviderDisabled(String s) {
        LocationProvider provider = locationManager.getProvider(s);
        Log.d("@@@@@@@@@@@@@@@@@@", "onProviderDisabled: Location Provider " + provider + " has been disabled");
    }
}
