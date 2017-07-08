package alpha.sunshine;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by marian on 07.07.2017.
 */

public class NetworkThread extends AsyncTask<String, Void, InputStream> {
    private MainActivity activity;

    public NetworkThread(MainActivity mainActivity) {
        this.activity = mainActivity;
    }


    String urlStr = "";       //TODO
    @Override
    protected InputStream doInBackground(String... strings) {
        InputStream stream = null;
        URL url = null;
        URLConnection conn = null;
        urlStr = strings[0];
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            Log.e("NetworkThread", "doInBackground: " + "URL is malformed!");
        }

        try {
            conn = url.openConnection();
        } catch (IOException e) {
            Log.e("NetworkThread", "doInBackground: " + "IOException 1");
            e.printStackTrace();
        }
        try {
            conn.connect();
        } catch (IOException e) {                                                                   //TODO crash on start without a network connection
            Log.e("NetworkThread", "doInBackground: " + "IOException 2");
            e.printStackTrace();
        }

        try {
            stream = conn.getInputStream();
        } catch (IOException e) {
            Log.e("NetworkThread", "doInBackground: " + "IOException 3");
            e.printStackTrace();
        }

        return stream;
    }
    //receives about 2kB per XML document
    @Override
    protected void onPostExecute(InputStream in) {
        activity.getXML(in);
    }
}
