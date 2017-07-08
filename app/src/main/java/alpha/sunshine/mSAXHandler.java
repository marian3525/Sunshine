package alpha.sunshine;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class mSAXHandler extends DefaultHandler{
    // do not call superclass methods, it will stop at the first tag

    private boolean isInTemperature = false;                                                      //true when it is inside the temperature tag

    /**
     * Receive notification of the beginning of the document.
     * <p/>
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the beginning
     * of a document (such as allocating the root node of a tree or
     * creating an output file).</p>
     *
     * @throws SAXException Any SAX exception, possibly
     *                      wrapping another exception.
     */
    @Override
    public void startDocument() throws SAXException {
        /*ToDo: implement startDocument and save the GPS coordinates in a sharedPrefs to use it when the device is offline
        The SAXException is thrown at xmlReader.parse(new InputSource(new StringReader(serverResponse)));
        and if the app reaches startDocument() it is known for sure that
        the current location has been mapped to a city by the server and
        can be used when the exception occurs
         */

    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Log.d("startElement", "|" +localName + "|");
        if(localName.equals("temperature")) {
            isInTemperature = true;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        Log.d("Characters: ", new String(ch, start, length));

        if(isInTemperature) {
            Log.e("Parsing Temp:", "characters: " + new String(ch, start, length) );
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        Log.d("endElement: ", "|" +localName + "|");
        if(localName.equals("temperature")) {
            isInTemperature = false;
        }
    }
}
