package edu.fsu.cs.mobile.top10;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

public class ParseApplication {
    private static final String TAG = "ParseApplication";
    private ArrayList<FeedEntry> applications;

    public ParseApplication() {
        this.applications = new ArrayList<>();
    }

    public ArrayList<FeedEntry> getApplications() {
        return applications;
    }


    //function returns a boolean value in the even issues arise from the parsing process
    //status is a toggle which will be set to false in the event exception is thrown
    //create a new feedentry for each entry in the xml file
    //inEntry verifies that data being processed is inside the entry tag itself
    //textValue holds the value of the current tag
    public boolean parseStringXMLData(String xmlData){
        boolean status = true;
        FeedEntry currentEntry = null;
        boolean inEntry = false;
        String textValue = "";

        try{
            /*lines 34 - 29
            * responsible for setting up the XML parser which will do the work
            * of interpreting the xml document
            * ---------------------------------
            * How to use the parser
            * 1) you can create instances of "pulParser objects at will
            *    you have to create a factory which is provided by the API
            *    factories are common and are used when we do not know exactly
            *    class we are using nor do we care we just want it to perform the requested
            *    task
            * 2) Pull Parser object is created and we tell them what to parse by creating a
            *    StringReader using the xml data as an argument; in this case "xmlData" string
            *    StringReader is a class which treats a string as a stream; it's an efficient way of
            *    treating strings as a stream and how the "XML Parser" does it's tasks
            *    THIS IS REQUIRED XMLPARSER REQUIRES A STRINGREADER WHICH REQUIRES A STRING
            * 3) As pull parser parses the data various events occur like reaching the end of a document
            *    we can watch for these various events and respond to them by checking for them
            * 4) The while loop is where the data parsing actual occurs; we will instantiate a new
            *    feedEntry object and continue to do so until EOF is reached*/
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlData));
            int eventType = xpp.getEventType();






            /*_________________________
            * While Loop
            * -------------------------
            * 1) start by getting the specific tag via "getName()"; note getName() can return
            *    null if not in a tag
            * 2) eventually when getName returns an actual tag, i.e., the StartTag, it will begin
            *    parsing the data so long as it is an entry tag; if not it is ignored along with
            *    the data
            * 3) if it is an ENTRY_TAG then we set inEntry to true and create a new instance
            *    of the FeedEntry class which we are going to put data into
            * 4) at the end of the loop we check the next event via the PullParsers "next()"
            *    method. This basically tells the parser to continue on until the next
            *    interesting thing comes about (another entry, EOF, etc.)*/

            while(eventType != XmlPullParser.END_DOCUMENT){

                String tagName = xpp.getName();

                switch (eventType){
                    case XmlPullParser.START_TAG:
                        Log.d(TAG, "parse: Starting tag for: " + tagName);
                        if("entry".equalsIgnoreCase(tagName)){
                            inEntry = true;
                            currentEntry = new FeedEntry();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        Log.d(TAG, "parse: Ending tag for: " + tagName);
                        if(inEntry){
                            if("entry".equalsIgnoreCase(tagName)){
                                applications.add(currentEntry);
                                inEntry = false;
                            } else if("name".equalsIgnoreCase(tagName)){
                                Log.d(TAG, "parseStringXMLData: textValue is: " + textValue);
                                currentEntry.setName(textValue);
                            } else if("artist".equalsIgnoreCase(tagName)){
                                currentEntry.setArtist(textValue);
                            } else if("releaseDate".equalsIgnoreCase(tagName)){
                                currentEntry.setReleaseDate(textValue);
                            } else if("summary".equalsIgnoreCase(tagName)){
                                currentEntry.setSummary(textValue);
                            } else if("image".equalsIgnoreCase(tagName)){
                                currentEntry.setImageURL(textValue);
                            }
                        }
                        break;
                    default:
                        //nothing to do
                }
                eventType = xpp.next();
            }

            for(FeedEntry app: applications){
                Log.d(TAG, "******************");
                Log.d(TAG, app.toString());
            }

        }catch (Exception e){
            status = false;
            e.printStackTrace();
        }

        return status;
    }
}
