package alpha.sunshine;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static BufferedReader in;

    //permission request codes in hex
    private static final int FINE_LOCATION_REQUEST = 0x1;
    private static final int COARSE_LOCATION_REQUEST = 0x2;
    private static final int INTERNET_REQUEST = 0x3;
    private static final int NETWORK_STATE = 0x4;
    //runtime permission status
    private static boolean isFineLocationAllowed;
    private static boolean isCoarseLocationAllowed;
    private static boolean isNetworkAllowed;
    private static boolean isNetworkCheckAllowed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updatePermissions();
        if(isFineLocationAllowed && isCoarseLocationAllowed && isNetworkAllowed && isNetworkCheckAllowed) {
            start();
        }
        else {
            //TODO dialog explaining that all permissions are needed for the app to work and request them again on a button click
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    private void start() {
        URLBuilder urlBuilder = new URLBuilder("metric", this);
        urlBuilder.build();    //TODO check for network (mandatory) and GPS (optional) first
    }

    public void onReceiveURL(String outputURL) throws IOException {                                 //called from URLBuilder.getURL() after receiving a nonNull location
        Log.d("############", "onReceiveURL: " + outputURL);

        if(isNetworkAvailable()) {
           new NetworkThread(this).execute(outputURL);
        }
        else {
            //TODO ask to user to enable a network
            //networkPopUp();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected void getXML(InputStream inputStream) {
        in = new BufferedReader(new InputStreamReader(inputStream));

        String inputLine = null;
        try {
            //the document is no longer an one liner, the second row is what we need to parse the XML data
            inputLine = in.readLine();
            inputLine = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String XMLString = inputLine;
        //XMLString.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");

        Log.i("XML:", XMLString);

        TextView xml = (TextView) findViewById(R.id.textView);
        xml.setText(XMLString);

        new Parser(XMLString, this).parse();                                                              //continue XML processing in mSAXHandler
    }

    public static String prettyFormat(String input, int indent) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e); // simple exception handling, please review it
        }
    }

    public void onFailedParse() {
        //called when a SAXException occurs. Usually the server cannot find the city and sends back an invalid XML document
        //TODO: when this occurs build a new URL using the last known good location stored in a sharedPref
        Log.e("Parser", "Parsing failed");
    }
    @TargetApi(23)
    private void updatePermissions() {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            }
            else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST);
            }
        }
        else {
            isFineLocationAllowed = true;
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

            }
            else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION_REQUEST);
            }
        }
        else {
            isCoarseLocationAllowed = true;
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {

            }
            else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET}, INTERNET_REQUEST);
            }
        }
        else {
            isNetworkAllowed = true;
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE)) {

            }
            else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_NETWORK_STATE}, NETWORK_STATE);
            }
        }
        else {
            isNetworkCheckAllowed = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_REQUEST: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted, proceed using GPS
                    isFineLocationAllowed = true;
                }
                else
                {
                    //boo, permission denied. Explain to the user that the location is necessary to provide accurate weather forecasts
                    isFineLocationAllowed = false;
                }
                break;
            }
            case COARSE_LOCATION_REQUEST: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted, proceed using network location
                    isCoarseLocationAllowed = true;
                }
                else {
                    //boo, permission denied. Explain to the user that the location is necessary to provide accurate weather forecasts
                    isCoarseLocationAllowed = false;
                }
                break;
            }
            case INTERNET_REQUEST: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //internet access granted
                    isNetworkAllowed = true;
                }
                else {
                    //internet access denied, ask the user to enable it
                    isNetworkAllowed = false;
                }
                break;
            }
            case NETWORK_STATE: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //may check for network
                    isNetworkCheckAllowed = true;
                }
                else {
                    //try again
                    isNetworkCheckAllowed = false;
                }
                break;
            }
        }
    }
}
