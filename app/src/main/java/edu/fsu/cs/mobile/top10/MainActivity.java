package edu.fsu.cs.mobile.top10;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    //for logging
    private static final String TAG = "MainActivity";
    private ListView listApps;
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int feedLimit = 10;
    private String feedCacheUrl = "INVALIDATED";
    public static final String STATE_URL = "feedUrl";
    public static final String  STATE_LIMIT = "feedLimit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //listApps is the List View holding the individual
        listApps = findViewById(R.id.xmlListView);

        if(savedInstanceState != null){
            feedUrl = savedInstanceState.getString(STATE_URL);
            feedLimit = savedInstanceState.getInt(STATE_LIMIT);
        }

        //string format more or less allows custom input and modification to a string
        //think "printf" from C
        downloadUrl(String.format(feedUrl, feedLimit));
       /*
        Log.d(TAG, "onCreate: Starting asynctask");
        DownloadData downloadData = new DownloadData();
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml");
        Log.d(TAG, "onCreate: Done!");
       */
    }


    /*
    * -this function is called when it time to inflate
    * the activity's menu
    * "create the menu objects from the xml file"
    *
    *  -initially, when we tried to inflate a view from the adapter, we had to get an inflator from a context
    *  -but an activity or appcompatactivity is a context so we can just call getMenuInflater and call inflate method
    *   and give it the resource id to the xml file containing the menu xml file
    *   return true to tell android the menu was inflated
    * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);
        if(feedLimit == 10){
            menu.findItem(R.id.top10).setChecked(true);
        } else{
            menu.findItem(R.id.top25).setChecked(true);
        }

        return true;
    }



    /*
    * -THIS function is called whenever an item is selected in the options menu
    * -androids parser passes in the item that was selected from the menu
    * -first we will grab the id so we know what to do with that specific option
    * -
    * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        switch(id){
            case R.id.menuFree:
                feedUrl ="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;
            case R.id.menuPaid:
                feedUrl ="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.menuSongs:
                feedUrl ="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.top10:
            case R.id.top25:
                if(!item.isChecked()){
                    item.setChecked(true);
                    feedLimit = 35 - feedLimit;
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + "setting feedLimit to " + feedLimit);
                } else {
                    Log.d(TAG, "onOptionsItemSelected: "+ item.getTitle() + "feedLimit unchanged");
                }
                break;
            case R.id.menuRefresh:
                feedCacheUrl = "INVALIDED";
                break;
            default:
                // possible to create submenus
                //when you go into those it makes a call to this method when the sub menu is opened
                return super.onOptionsItemSelected(item);
        }

        //once the url is changed
        //we will do the same thing we do in the oncreate method
        //create a new downloaddata object and call it's execute method with a new url
        downloadUrl(String.format(feedUrl, feedLimit));
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        outState.putString(STATE_URL, feedUrl);
        outState.putInt(STATE_LIMIT, feedLimit);
        super.onSaveInstanceState(outState);

    }


    private void downloadUrl(String feedUrl){
        if(!feedUrl.equalsIgnoreCase(feedCacheUrl)){
            Log.d(TAG, "downloadURL: Starting asynctask");
            DownloadData downloadData = new DownloadData();
            downloadData.execute(feedUrl);
            Log.d(TAG, "downloadUrl: Done!");
        } else{
            Log.d(TAG, "downloadUrl: URL NOT CHANGED");
        }

      
    }

    //created inner class because nothing else will be using the ASNYC Class
    //an "inner" class
    //created a private class which extends the Async class
    //the Async class has 3 templated parameters String, Void, String
    //the information passed into the task is of type String; pass url for the  rss feed(1)
    // the second parameter is used to display a progress bar "Void" (2) but the program is small so no
    //Void means do use this
    //the third parameter is the type of the result we want to get back
    //all the xml which we are retrieving from the rss feed is a string, so we are going to
    //get it back as a string
    //1 -> contains the address of rss feed
    //2 -> not showing a progress bar
    //3 -> string containing all xml after downloaded
    private class DownloadData extends AsyncTask<String, Void, String>{


        //for logging
        private static final String TAG = "DownloadData";

        //this function tells android what code to run in the background
        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: starts with " + strings[0]);
            String rssFeed = downloadXML(strings[0]);

            if(rssFeed == null){
                Log.e(TAG, "doInBackground: Error downloading");
            }

            return rssFeed;
        }

        //we want to get data when the task completes (the task of the other thread)
        //this function will be called once the task is completed (grabbing the data)
        //without this we would never know the task was finished and would not get the data
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parameter is " + s);
            ParseApplication parseApplication = new ParseApplication();
            parseApplication.parseStringXMLData(s);


            /*STEPS TO USING AN ARRAYADAPTER
            * 1) add a listview/recycler to layout
            * 2) create a textview resource
            * 3) create an instance of an adapter
            * 4) call "setadapter" to link listview to the adapter
            * */





            //arrayAdapter object
            //3 parameters
            //1 -> "Context"
            //2 -> "resource containing source view with the text widget"
                    //that the array adapter will put the data into
            //3 -> //where the data is coming from (the array with all the feedEntries

          /*  ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(MainActivity.this,
                                                   R.layout.list_item, parseApplication.getApplications());

            //ListView is told which adapter to use to get it's data
            listApps.setAdapter(arrayAdapter);
          */

            FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this, R.layout.list_record,
                                                      parseApplication.getApplications());
            listApps.setAdapter(feedAdapter);

        }

        //this function will start by opening an http connection to access the stream of data
        //over the internet from the url
        //connection prpvide an inputstream; use an inputstream reader to takemn incoming data
        //from the stream
        //when the stream device is slow, it's best to use a bufferedReader
        //the bufferedReader buffers the data coming in from the stream
        //so instead of repeatedly accessing the network or harddrive, a block of data is read into
        //memory and a program can then read and parse from the buffer

        private String downloadXML(String urlPath){
            //appending to a string alot as we read data from inputStream; more efficient
            StringBuilder xmlResult= new StringBuilder();

            //inside try block because connection to the url is not guranteed
            try{
                //URL created with string passed into the function
                URL url = new URL(urlPath);

                //open the url connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                //get the response code and log to verify status of the connection
                int response = connection.getResponseCode();

                Log.d(TAG, "downloadXML: the response code was" + response);

                //alternative method for input (chaining)
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int charsRead;
                char[] inputBuffer = new char[500]; //increase number size by increasing the number size

                //this loop reads the inputstream until EOF is reached
                while(true){
                    charsRead = bufferedReader.read(inputBuffer);

                    //if at EOF we break out of the loop and close the stream reader
                    if(charsRead < 0){
                        break;
                    }

                    //if characters are being read, append to xmlResult StringBuilder
                    //parameters
                    //1->
                    //2->
                    //3->
                    if(charsRead > 0){
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }

                bufferedReader.close();

                return xmlResult.toString();

            } catch(MalformedURLException e){
                Log.e(TAG, "downloadXML: Invalid URL" + e.getMessage());
            } catch(IOException e) {
                Log.e(TAG, "downloadXML: IO Exception reading data: " + e.getMessage());
            } catch(SecurityException e){
                Log.e(TAG, "downloadXML: Security Exception. Needs Permission? " + e.getMessage() );
                e.printStackTrace();
            }

            return null;
        }
    }
}

