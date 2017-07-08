package alpha.sunshine;

import android.location.Location;
import android.location.LocationManager;
import android.util.Pair;

import java.io.IOException;
/**
 * Created by marian on 07.07.2017.
 */

public class URLBuilder {
    private static final String APP_ID = "appid=9f8319fcfe1b006df64b89c9ce26e1cc";
    private static String baseUrl = "http://api.openweathermap.org/data/2.5/weather?";              //+ "lat=12.34&lon=56.78&mode=xml"
    private static String units;
    private static MainActivity activity;
    private static double lat, lon;
    private static String outputURL = "";
    protected Location lastLocation;
    LocationFinder finder;

    public URLBuilder(String units, MainActivity activity) {                                        //units: metric/imperial
        URLBuilder.units = units;
        URLBuilder.activity = activity;


    }

    public URLBuilder(String units, MainActivity activity, Pair<Double, Double> checkedCoordinates) {
        URLBuilder.units = units;
        URLBuilder.activity = activity;
        lat = checkedCoordinates.first;
        lon = checkedCoordinates.second;
    }


    public void build() {
        finder = new LocationFinder(activity, this);

        lastLocation = finder.getLocation();
        if (lastLocation != null) {
            getURL(lastLocation);
        }
    }

    public void build(Location location) {  //only called by onLocationChanged with an updated location, do not call it outside the callback, it will break the timing
            getURL(location);
    }


    public void getURL(Location location) {

        outputURL = "";
        outputURL += baseUrl;
        outputURL += APP_ID;
        outputURL += "&units=";

        outputURL += units;

        outputURL += "&mode=xml";

        lat = location.getLatitude();                                                             //47.651389;
        int lat_temp = (int) (lat * 100);
        lat = lat_temp / 100D;

        lon = location.getLongitude();                                                            //26.255556;
        int lon_temp = (int) (lon * 100);
        lon = lon_temp / 100D;

        outputURL = outputURL + "&lat=" + String.valueOf(lat) + "&lon=" + String.valueOf(lon);
        try {
            activity.onReceiveURL(outputURL);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
