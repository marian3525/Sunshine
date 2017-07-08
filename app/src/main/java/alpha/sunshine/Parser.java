package alpha.sunshine;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by marian on 07.07.2017.
 */

public class Parser {
    private SAXParser parser;
    private XMLReader xmlReader;
    private String serverResponse;
    private MainActivity mainActivity;

    public Parser(String serverResponse, MainActivity mainActivity) {

        this.serverResponse = serverResponse;
        this.mainActivity = mainActivity;

        try {
            init();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void init() throws ParserConfigurationException, SAXException {
        parser = SAXParserFactory.newInstance().newSAXParser();

        xmlReader = parser.getXMLReader();

        mSAXHandler saxHandler = new mSAXHandler();
        xmlReader.setContentHandler(saxHandler);
    }

    public void parse() {                                                                           //the xml is processed using the callbacks implemented in mSAXHandler
        try {
            xmlReader.parse(new InputSource(new StringReader(serverResponse)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
            mainActivity.onFailedParse();
        }
    }
}
