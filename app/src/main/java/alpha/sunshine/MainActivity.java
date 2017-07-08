package alpha.sunshine;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public class MainActivity extends AppCompatActivity {
    private static BufferedReader in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start();
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
            //TODO ask to user to enable a network connection
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

        new Parser(prettyFormat(XMLString, 4), this).parse();                                                              //continue XML processing in mSAXHandler
    }

    public static String prettyFormat(String input, int indent) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            //transformerFactory.setAttribute("indent-number", indent);
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
}
