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
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;

import com.geeky7.rohit.flash_a.CONSTANT;
import com.geeky7.rohit.flash_a.DirectionsJSONParser;
import com.geeky7.rohit.flash_a.Main;
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

    Main m;
    static Context context;

    Geocoder geocoder;
    List<Address> addresses;
    SharedPreferences preferences;

    String sender;
    String placeName;
    String durationEta;
    String URL;
    int counter = 0;

    public LocationService() {
    }

    @Override
    public void onCreate() {
//        super.onCreate();
        m = new Main(getApplicationContext());
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        sender = preferences.getString(CONSTANT.SENDER,CONSTANT.SENDER);

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
    // stop location update when no longer needed
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
        boolean gps = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        try {
            // if the location service and internet is on get that address and start places code
            if (gps/*&&internet*/){
                // if location null, get last known location, updating the time so that we don't show quite old location
                Log.i(TAG,"GPS on");
                if (mCurrentLocation==null){
                    Log.i(TAG,"location null");
                    Thread.sleep(2000);
                    Thread.sleep(2000);
                    Thread.sleep(2000);
                    Log.i(TAG,"Slept for 6 seconds");
                    mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                }

                if (mCurrentLocation!=null){
                    Log.i(TAG," Location is not null");
                    addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                    Log.i(TAG," after address fetched. Calling code");
    //              initiates places code to fetch the name of the nearby place
                    placesCode();
                    Log.i(TAG," Places code called");
                    etaCode();
                    Log.i(TAG," ETA code called");
                    sendBroadcast();
                    Log.i(TAG," Broadcasted");
                }
                // stop itself after message is sent
                stopSelf();
            }
            // when gps if off
            // registers a receiver when the status of the gps changes
            else{
                Log.i(TAG, "gps off");
                String name = sender;
                if (checkContactPermission()) name = getContactName(sender,getApplicationContext());
                m.pugNotification("Location Request from "+name,"Turn GPS on","");

                getApplicationContext().registerReceiver(gpsReceiver,
                        new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean gps = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean internet = m.isNetworkAvailable();

        //Do your stuff on GPS status change
            try {
                if(!mGoogleApiClient.isConnected())
                    stopSelf();
                Thread.sleep(2000);
                if(gps&&internet){
                    if (mCurrentLocation==null){
                        Thread.sleep(2000);
                        Thread.sleep(2000);
                        Thread.sleep(2000);
                        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    }
                    if (mCurrentLocation!=null){
                        addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                        Thread.sleep(2000);
                        Thread.sleep(2000);
                        Thread.sleep(2000);
                        placesCode();
                        etaCode();
                        sendBroadcast();
                    }
                }
                else
                    Log.i(TAG,"Gps status has changed but something is wrong. find me at loc 253");
                stopSelf();
                // register a broadcast receiver - for whenever the gps is turned on/off
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
        SmsManager manager = SmsManager.getDefault();

        updateLogAndToast("sendSMS");
        // sender contains the phone number
        String name = sender;
        // if the contact permission is granted get the name of the contact
        // else name = phoneNumber; use that in notification
        // 'else' would also run when the number is not saved in the contact list Yeah I could think of this scenario
        if (checkContactPermission())
            name = getContactName(sender,getApplicationContext());

        String address = getAddress();

        boolean sendEta = preferences.getBoolean(getResources().getString(R.string.settings_send_eta),false);
        boolean landmark = preferences.getBoolean(getResources().getString(R.string.settings_landmark),false);

        boolean send_eta_if_contact_selected = preferences.getBoolean(CONSTANT.SEND_ETA_IF_CONTACT_SELECTED,false);

        Log.i(TAG,"sendSMS"+etaS+" "+ sendEta+" "+ send_eta_if_contact_selected);


        if (!etaS.equals("NA")&&sendEta&&send_eta_if_contact_selected) etaS = " ETA home "+etaS+".";
        else etaS= "";

        if (!landmark)
            placeS = "";
        else{
            placeS = "Near "+placeS;
            address = " ("+ address + ").";
        }

        String message = placeS+ address+ etaS+" " +URL;
        manager.sendTextMessage(sender,null, message, null, null);

        boolean notification = preferences.getBoolean(getResources().getString(R.string.settings_notification),false);
        if (notification)
            m.pugNotification("Location shared","Your current location shared with",name);
    }

    // checks if the contact permission is granted or not
    public boolean checkContactPermission(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
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
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();

        String address = addresses.get(0).getAddressLine(0)
                .replace(state,"").replaceFirst(country,"").replaceFirst(postalCode,"").replaceAll(",","").trim();

        Log.i(TAG+""+" Address",address);

//        String street = addresses.get(0).getFeatureName();
//        String city = addresses.get(0).getLocality();
//        String number = addresses.get(0).getFeatureName();
//        String council = addresses.get(0).getSubAdminArea();
//        null- getSubLocality(),getPremises(),getThoroughfare()

//        Log.i("address","City: "+city);
//        Log.i("address","State: "+state);
//        Log.i("address","Country: "+country);
//        Log.i("address","street: "+street);
//        return street+", "+ city;
        return address;
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
        String key = getApplicationContext().getString(R.string.API_KEY_GEO );
        // certain place types
        String types = "cafe|amusement_park|university|stadium|shopping_mall|restaurant";

        String location = "https://www.google.com/maps/search/?api=1&query=" + mLatitude + "," + mLongitude;
        URL = location;

//        URL = String.valueOf(new newShortAsync().execute(location));
        // to avoid running shortner code on async task
        if (android.os.Build.VERSION.SDK_INT > 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //calling a direct method which runs urlShortner code
        URL = m.urlShortner(location);



        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + mLatitude + "," + mLongitude);
        sb.append("&radius="+mRadius);
        sb.append("&types=" +  URLEncoder.encode("point_of_interest", "UTF-8"));
        sb.append("&sensor=true");
        sb.append("&key=" + key);
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

    // sets the name of the place found in a global variable and increments a counter which counts if both the async tasks are finished- to sendSMS
    // called from onPostExecute of parser task
    public void setPlaceName(String s){
        placeName = s;
        counter++;
        bothAsync();
    }

    //sets the calculated eta to global variable and increments a counter which counts if both the async tasks are finshed - to sendSMS
    // called from onPostExecute parserTask for eta
    public void setDurationEta(String s){
        durationEta = s;
        counter++;
        bothAsync();
    }

    // checks if both async tasks are completed if so sendSMS
    public void bothAsync(){
        updateLogAndToast("BothAsync "+counter);
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
                setPlaceName(name);
                updateLogAndToast("Places "+name);
            }
        }
    }

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
                if (!jPlace.isNull("name")) placeName = jPlace.getString("name");
                // Extracting Place Vicinity, if available
                if (!jPlace.isNull("vicinity")) vicinity = jPlace.getString("vicinity");

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

    //this method calls the eta async task
    public void etaCode(){
        // Getting URL to the Google Directions API
        String url = buildEtaURL();
        // Start downloading json data from Google Directions API
        if (url.equals("")) setDurationEta("NA");
        else new DownloadTask().execute(url);
    }
    // called to build eta url from etaCode
    public String buildEtaURL(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String dest = preferences.getString(CONSTANT.HOME_ADDRESS,"");

        String origin = getAddress();


        if(dest.equals("")||dest.equals(R.string.home_address_text))
            return "";

        String str_origin = "origin="+origin;
        String str_dest = "destination="+dest;
        String sensor = "sensor=false";
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        Log.i(TAG+ " ETAURL",url);
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

    // sends a broadcast
    // called when the eta and places code have been called
    // purpose is to send address and place name to
    private void sendBroadcast (){
        Intent intent = new Intent ("message"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra(CONSTANT.ADDRESS,getAddress());
        intent.putExtra(CONSTANT.PLACE_NAME,placeName);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //updated the log and the toast message
    //why is this method here? it helps to suppress all the toast messaged used during the testing by just commenting one LOC
    public void updateLogAndToast(String s){
        Log.i(TAG,s);
//        Main.showToast(s);
    }

}