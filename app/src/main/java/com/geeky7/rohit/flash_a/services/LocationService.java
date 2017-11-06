//This is the core class which gets the location convert it into address and then find a close placeOfInterest
// compiles a message and sends it to the sender of the message
// Yeah that's a lot of work
package com.geeky7.rohit.flash_a.services;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;

import com.geeky7.rohit.flash_a.CONSTANT;
import com.geeky7.rohit.flash_a.DirectionsJSONParser;
import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.MyApplication;
import com.geeky7.rohit.flash_a.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class LocationService extends Service implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,LocationListener{

    public static final String TAG = CONSTANT.LOCATION_SERVICE;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS/2;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mlocationRequest;
    private Location mCurrentLocation;


//    private static final int GOOGLE_API_CLIENT_ID = 0;
//    private boolean mRequestingLocationUpdates;
//    private String mLastUpdateTime;

//    private boolean googleApiClientConnected;
    static Context context;

    Main m;

    Geocoder geocoder;
    List<Address> addresses;

    String string = "Null";
    SharedPreferences preferences;
    String sender = "";


    String placeName;
    String durationEta;

    int counter = 0;
    public LocationService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        sender = preferences.getString("sender","");

        m = new Main(getApplicationContext());

        buildGoogleApiClient();
        mGoogleApiClient.connect();

        // googleAPI is connected and ready to get location updates- start fetching current location
        if(mGoogleApiClient.isConnected())
            startLocationupdates();

        // get address from the lat lng
        geocoder = new Geocoder(this, Locale.getDefault());


        Log.i(TAG,"LocationService Created");
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");

        stopSelf();

        if (mGoogleApiClient.isConnected())
            stopLocationupdates();

        mGoogleApiClient.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    // add the API and builds a client
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(LocationService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    // fetched location every `UPDATE_INTERVAL_IN_MILLISECONDS` milliseconds
    // since the service destroys after fetching location once it is of little use; I mean the interval of fetching the location

    private void createLocationRequest() {
        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mlocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // requests a location
    protected void startLocationupdates() throws SecurityException {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mlocationRequest, this);
    }
    // stop location update; no longer needed
    protected void stopLocationupdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mGoogleApiClient.connect();
    }
    // when the googleApiClient and set to go this method is called
    // it fetches the location and then builds the places url
    // and finally initiate the code for actually finding the nearby place

    @Override
    public void onConnected(Bundle bundle)throws SecurityException {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean b = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // if location null, get last known location, updating the time so that we don't show quite old location
        if (mCurrentLocation==null){
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            try {
                // if the location service is on get that address and start places code
                if (b){
                    addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
//                    sendBroadcast();

//                    initiates places code to fetch the name of the nearby place
                    placesCode();
                    etaCode();
                    // stop itself after message is sent
                    stopSelf();
                }
                // when gps if off
                // registers a receiver when the status of the gps changes
                else{
                    String name = sender;
                    if (checkContactPermission()){
                        name = getContactName(sender,getApplicationContext());
                    }
                    Log.i(TAG, "gps off");
                    m.pugNotification("Location Request from "+name,"Turn GPS on","");

                    getApplicationContext().registerReceiver(gpsReceiver,
                            new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }
    }
    // whenever the gps status changes this code would run
    // however we are only concerned when it is turned on
    // this method contains similar code to if statement so maybe one day I'll put the common code in a method
    // and just call that method
    // Updated: Don't worry already done it :D
    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                //Do your stuff on GPS status change
                try {
                    if(!mGoogleApiClient.isConnected())
                        stopSelf();
                    Thread.sleep(2000);
                    // in case the app force closes when the gos is off and turned on later
                    // uncomment the below code
                    if (mCurrentLocation==null){
                        /*Thread.sleep(2000);
                        Thread.sleep(2000);*/
                        Thread.sleep(2000);
                        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    }
                    if (mCurrentLocation!=null)
                        addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                    placesCode();
                    etaCode();
                    stopSelf();

                    // register a broadcast receiver - for whenver the gos is turned on/off
                    // we do some work when it's status is turned on
                    getApplicationContext().unregisterReceiver(gpsReceiver);
                }
                 catch(IOException e) {
                    e.printStackTrace();
                }
                catch (SecurityException se){
                    se.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    // Magic method to send the SMS to the sender
	// called from ParserTask class.onPostExecute when the name of the place is fetched
    private void sendSMS(String placeS, String etaS) {
        updateLogAndToast("sendSMS 2");
        // sender contains the phone number
        String name = sender;
        // if the contact permission is granted get the name of the contact
        // else name = phoneNumber use that in notification
        // else would also run when the number is not saved in the contact list
        if (checkContactPermission())
            name = getContactName(sender,getApplicationContext());

        String address = getAddress();
//        String eta = EtaMethodUnUsed();

        SmsManager manager = SmsManager.getDefault();

        if (!etaS.equals("NA")) etaS = " ETA home "+etaS;
        else etaS= "";

        String message = "I am near "+ placeS+ " ("+ address + ")"+ etaS;
        manager.sendTextMessage(sender,null, message, null, null);

        boolean noti = preferences.getBoolean("notification",true);
        if (noti)
            m.pugNotification("Location shared","Your current location shared with",name);
    }

    // checks if the contact permission is granted or not
    public boolean checkContactPermission(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG+""+"contactPermission","false");
            return false;
        }
        Log.i(TAG+""+"contactPermission","true");
        return true;
    }

    // gets the contact name if the permission is granted
    // else the method in not called
    public String getContactName(final String phoneNumber,Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName = "";
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
                cursor.close();
            }
        }
            return contactName;
    }

    // this method gets the address and lets you make selection what parameters of address to include
    private String getAddress() {
        for (int i = 0; i< addresses.size();i++)
            Log.i(TAG+""+"All addresses",addresses.get(i).getAddressLine(i));

        String address = addresses.get(0).getAddressLine(0);

        return address;

//        String city = addresses.get(0).getLocality();
//        String state = addresses.get(0).getAdminArea();
//        String country = addresses.get(0).getCountryName();
//        String postalCode = addresses.get(0).getPostalCode();
//        String knownName = addresses.get(0).getFeatureName(); // unit 32

//        String s1 = addresses.get(0).getSubLocality(); // null
//        String s2 = addresses.get(0).getPremises(); // null
//        String s3 = addresses.get(0).getThoroughfare(); //null
//        String s4 = addresses.get(0).getSubAdminArea(); city of west torrens

    }

    // this code starts with building the places URL and then call the actual places code
    private void placesCode() {
        String sb = null;
        try {
            sb = buildPlacesURL().toString();
            new PlacesTask().execute(sb);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    // builds the url for fetching the nearby places
    public StringBuilder buildPlacesURL() throws UnsupportedEncodingException {
        double mLatitude = mCurrentLocation.getLatitude();
        double mLongitude = mCurrentLocation.getLongitude();
        int mRadius = 500;

        String number1 = getApplicationContext().getString(R.string.API_KEY);

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + mLatitude + "," + mLongitude);
        sb.append("&radius="+mRadius);
        sb.append("&types=" +  URLEncoder.encode("point_of_interest", "UTF-8"));
        sb.append("&sensor=true");
        sb.append("&key=" + number1);
        Log.i(TAG+""+"Places", sb.toString());
        return sb;
    }

    // download the data from the URL
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();

        } catch (Exception e) {
        } finally {
            if (iStream != null) {
                iStream.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return data;
    }


    public void setPlaceName(String s){
        placeName = s;
        counter++;
        bothAsync();
    }

    public void setDurationEta(String s){
        durationEta = s;
        counter++;
        bothAsync();
    }

    public void bothAsync(){
        updateLogAndToast("BothAsync"+counter);
        if(counter==2)
            sendSMS(placeName,durationEta);
    }


    // Parsing the data received
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        Context mContext;
        public ParserTask(Context context){
            mContext = context;
        }
        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            Place_JSON placeJson = new Place_JSON();

            try {
                jObject = new JSONObject(jsonData[0]);

                places = placeJson.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {

            if (!list.isEmpty()){
                HashMap<String, String> hmPlace = list.get(0);
                String name = hmPlace.get("place_name");
                //sendSMS(name);
                setPlaceName(name);
                updateLogAndToast("Places "+name);
            }
        }
    }// onPostExecute
//}// end of the parserTask class

    // gets the details of the place to let you choose which parameters of place you want to show
   public class Place_JSON {

        /**
         * Receives a JSONObject and returns a list
         */
        public List<HashMap<String, String>> parse(JSONObject jObject) {

            JSONArray jPlaces = null;
            try {
                jPlaces = jObject.getJSONArray("results");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            /** Invoking getPlaces with the array of json object
             * where each json object represent a place
             */
            return getPlaces(jPlaces);
        }

        private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
            int placesCount = jPlaces.length();
            List<HashMap<String, String>> placesList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> place = null;

            /** Taking each place, parses and adds to list object */
            for (int i = 0; i < placesCount; i++) {
                try {
                    /** Call getPlace with place JSON object to parse the place */
                    place = getPlace((JSONObject) jPlaces.get(i));
                    placesList.add(place);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return placesList;
        }
        private HashMap<String, String> getPlace(JSONObject jPlace){

            HashMap<String, String> place = new HashMap<String, String>();
            String placeName = "-NA-";
            String vicinity = "-NA-";
            String latitude = "";
            String longitude = "";
            String reference = "";
            String placeType = "";

            try {
                // Extracting Place name, if available
                if (!jPlace.isNull("name")) {
                    placeName = jPlace.getString("name");
                }

                // Extracting Place Vicinity, if available
                if (!jPlace.isNull("vicinity")) {
                    vicinity = jPlace.getString("vicinity");
                }

                latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
                reference = jPlace.getString("reference");
                placeType = jPlace.getString("types");

                place.put("place_name", placeName);
                place.put("vicinity", vicinity);
                place.put("lat", latitude);
                place.put("lng", longitude);
                place.put("reference", reference);
                place.put("types", placeType);

//                Log.i(TAG+""+"PlaceType",placeType);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return place;
        }
    }
    // calls download url method above and later starts parser class
    public class PlacesTask extends AsyncTask<String, Integer, String> {
        String data = null;
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.i(TAG+""+"Background Task", e.toString());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask(context);
            parserTask.execute(result);
            Log.i(TAG+""+"PlacesTaskOnPostExecute", result + "");
        }
    }

    /*
    ETA AsyncTask code
     */

    public void etaCode(){

        // Getting URL to the Google Directions API
        String url = buildEtaURL();
        // Start downloading json data from Google Directions API
        if (url.equals("")) setDurationEta("NA");
        else new DownloadTask().execute(url);
    }
    public String buildEtaURL(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String dest = preferences.getString("homeAddress","");

        String origin = getAddress();


        if(dest.equals(""))
            return "";

        String str_origin = "origin="+origin;
        String str_dest = "destination="+dest;
        String sensor = "sensor=false";
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        Log.i(TAG,url);
        return url;
    }

    /** A method to download json data from url */
    private String downloadUrlETA(String strUrl) throws IOException {
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
                data = downloadUrlETA(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Invokes the thread for parsing the JSON data
            ParserTaskETA parserTaskETA = new ParserTaskETA(context);
            parserTaskETA.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTaskETA extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {

        Context mContext;
        public ParserTaskETA(Context context){
            mContext = context;
        }
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
//            String distance = "";
            String duration;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    // Get distance from the list
                    if (j == 0) {
//                        distance = (String) point.get("distance");
                    }
                    // Get duration from the list
                    else if (j == 1) {
                        duration = point.get("duration");

                        setDurationEta(duration);
                        updateLogAndToast("ETA "+duration);
                    }
                }//end for - ith route
            }// end for- al routes
            setDurationEta("NA");
        }// end onPostExecute

    }

    private void sendBroadcast (){
        Intent intent = new Intent ("message"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra(CONSTANT.ADDRESS,getAddress());

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void updateLogAndToast(String s){
        Log.i(TAG,s);
//        Main.showToast(s);
    }
    public String startServices(){
//        startService(new Intent(this,LocationService.class));

        startService(new Intent(MyApplication.getAppContext(),LocationService.class));
        return getAddress();
    }

}