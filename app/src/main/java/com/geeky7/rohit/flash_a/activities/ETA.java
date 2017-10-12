package com.geeky7.rohit.flash_a.activities;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.geeky7.rohit.flash_a.MyApplication;
import com.geeky7.rohit.flash_a.services.LocationService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;

public class ETA {

    SharedPreferences preferences;
    String eta = "";
    public String eta(String origin,String dest){
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, dest);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        String eta1 = null;
        try {
            eta1 = downloadTask.execute(url).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return eta1;
    }
    public String getDirectionsUrl(String origin,String dest){


        // Origin of route
        String str_origin = "origin="+origin;

        // Destination of route
        String str_dest = "destination="+dest;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("ExceptionDownloadingURL", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, String, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);


            String distance = "";
            String duration = "";

            List<List<HashMap<String, String>>> result1 = new List<List<HashMap<String, String>>>() {
                @Override
                public int size() {
                    return 0;
                }

                @Override
                public boolean isEmpty() {
                    return false;
                }

                @Override
                public boolean contains(Object o) {
                    return false;
                }

                @NonNull
                @Override
                public Iterator<List<HashMap<String, String>>> iterator() {
                    return null;
                }

                @NonNull
                @Override
                public Object[] toArray() {
                    return new Object[0];
                }

                @NonNull
                @Override
                public <T> T[] toArray(@NonNull T[] ts) {
                    return null;
                }

                @Override
                public boolean add(List<HashMap<String, String>> hashMaps) {
                    return false;
                }

                @Override
                public boolean remove(Object o) {
                    return false;
                }

                @Override
                public boolean containsAll(@NonNull Collection<?> collection) {
                    return false;
                }

                @Override
                public boolean addAll(@NonNull Collection<? extends List<HashMap<String, String>>> collection) {
                    return false;
                }

                @Override
                public boolean addAll(int i, @NonNull Collection<? extends List<HashMap<String, String>>> collection) {
                    return false;
                }

                @Override
                public boolean removeAll(@NonNull Collection<?> collection) {
                    return false;
                }

                @Override
                public boolean retainAll(@NonNull Collection<?> collection) {
                    return false;
                }

                @Override
                public void clear() {

                }

                @Override
                public List<HashMap<String, String>> get(int i) {
                    return null;
                }

                @Override
                public List<HashMap<String, String>> set(int i, List<HashMap<String, String>> hashMaps) {
                    return null;
                }

                @Override
                public void add(int i, List<HashMap<String, String>> hashMaps) {

                }

                @Override
                public List<HashMap<String, String>> remove(int i) {
                    return null;
                }

                @Override
                public int indexOf(Object o) {
                    return 0;
                }

                @Override
                public int lastIndexOf(Object o) {
                    return 0;
                }

                @Override
                public ListIterator<List<HashMap<String, String>>> listIterator() {
                    return null;
                }

                @NonNull
                @Override
                public ListIterator<List<HashMap<String, String>>> listIterator(int i) {
                    return null;
                }

                @NonNull
                @Override
                public List<List<HashMap<String, String>>> subList(int i, int i1) {
                    return null;
                }
            };
            for(int i=0;i<result1.size();i++){

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if(j==0){    // Get distance from the list
                        distance = (String)point.get("distance");
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");

                        eta = duration;
                        preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("eta",duration);

                        editor.commit();

                    }
                }
            }
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            String distance = "";
            String duration;


            // Traversing through all the routes
            for(int i=0;i<result.size();i++){

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if(j==0){    // Get distance from the list
                        distance = (String)point.get("distance");
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        LocationService locationService = new LocationService();
                        locationService.value(duration);

                        eta = duration;
                        preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("eta",duration);

                        editor.commit();

                    }
                }
            }
        }
    }
}