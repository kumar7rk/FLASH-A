/*
* This class gets the location convert it into address and find a landmark
* code only used by the action bar button- current location
* class is called when the gps and internet is on and the app is opened
* is adpated from locationService class
* */

package com.geeky7.rohit.flash_a.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.geeky7.rohit.flash_a.CONSTANT;
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


public class LocationService2 extends Service implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,LocationListener{

    public static final String TAG = CONSTANT.LOCATION_SERVICE2;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS/2;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mlocationRequest;

    private Location mCurrentLocation;
    private Geocoder geocoder;

    static Context context;

    Main m;

    List<Address> addresses;

    SharedPreferences preferences;

    String sender = "";
    String placeName;
    private String address;
    String URL;
    public LocationService2() {
    }

    @Override
    public void onCreate() {
//        super.onCreate();
        m = new Main(getApplicationContext());

        m.calledMethodLog(TAG,"onCreate");

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        sender = preferences.getString(CONSTANT.SENDER,"");


        buildGoogleApiClient();
        mGoogleApiClient.connect();

        // googleAPI is connected and ready to get location updates- start fetching current location
        if(mGoogleApiClient.isConnected())
            startLocationupdates();

        // get address from the lat lng
        geocoder = new Geocoder(this, Locale.getDefault());

        m.updateLog(TAG,"LocationService2 Created");
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        m.calledMethodLog(TAG,"onBind");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        m.calledMethodLog(TAG,"onConnectionFailed");
        m.updateLog(TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        m.calledMethodLog(TAG,"onDestroy");
        stopSelf();
        if (mGoogleApiClient.isConnected()) stopLocationupdates();
        mGoogleApiClient.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        m.calledMethodLog(TAG,"onStartCommand");
        return START_NOT_STICKY;
    }

    // add the API and builds a client
    private void buildGoogleApiClient() {
        m.calledMethodLog(TAG,"buildGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(LocationService2.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    // fetched location every `UPDATE_INTERVAL_IN_MILLISECONDS` milliseconds
    // since the service destroys after fetching location once it is of little use; I mean the interval of fetching the location

    private void createLocationRequest() {
        m.calledMethodLog(TAG,"createLocationRequest");
        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mlocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // requests a location
    protected void startLocationupdates() throws SecurityException {
        m.calledMethodLog(TAG,"startLocationupdates");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mlocationRequest, this);
    }
    // stop location update; no longer needed
    protected void stopLocationupdates(){
        m.calledMethodLog(TAG,"stopLocationupdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        m.calledMethodLog(TAG,"onStart");
        mGoogleApiClient.connect();
    }
    // when the googleApiClient and set to go this method is called
    // it fetches the location and then builds the places url
    // and finally initiate the code for actually finding the nearby place

    @Override
    public void onConnected(Bundle bundle)throws SecurityException {
        m.calledMethodLog(TAG,"onConnected");
        m.updateLog(TAG+ " onConnected 1 ","Let's do it.");
//        boolean internet = m.isNetworkAvailable();
//        Main.showToast(internet+"");
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        try {
            // if the location service is on get that address and start places code
            // not checking for internet because to connect to playServices the internet is required anyway. Simple logic.
            // but still we have internet in comment- next time you find this comment and there has been no crash because of this
            // delete this comment and relevant code too
            if (gps/*&&internet*/){
                m.updateLog(TAG+ " onConnected 2 ","GPS is available");
                // if location null, get last known location, updating the time so that we don't show quite old location
                if (mCurrentLocation==null){
                    m.updateLog(TAG+ " onConnected 3 ","Apparently location was null");
                    mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    m.updateLog(TAG+ " onConnected 4 ","Fetched lastlocation");
                }
                if (mCurrentLocation!=null){
                    m.updateLog(TAG+ " onConnected 5 ","This says location is not null.");
                    addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                    m.updateLog(TAG+ " onConnected 6 ","addresses is fetched as well");
//                initiates places code to fetch the name of the nearby place
                    m.updateLog(TAG+ " onConnected 7 ","calling places code now");
                    placesCode();
                }
                m.updateLog(TAG+ " onConnected 8 ","This is the end of gps if statement.");
            }
            // when gps if off- register a receiver listening for status of the gps to change
            else{
                m.updateLog(TAG+ " onConnected 9 ","If you're here it means that the gps is off. You shouldn't be here.");
                getApplicationContext().registerReceiver(gpsReceiver,
                        new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
            }
        } catch (Exception e) {
            e.printStackTrace();
            m.updateLog(TAG + " onConnected 10 "," Some exception "+ e.getMessage());
        }
        m.updateLog(TAG+ " onConnected 11 ","End of me.");
    }
    // Whenever the gps status changes this code would run. We're only interested when it's turned on
    /* This method contains similar code to if statement so maybe one day I'll put the common code in a method
        and just call that method
        Updated: Don't worry already done it :D
     */
    // The above comment is legacy so I must keep it. And one day I'll become legacy too and there would be comments about me being kept. #OneDay

    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Do your stuff on GPS status change
            m.updateLog(TAG+" gps BroadcastReceiver ","This is the broadcastReceiver for gps status change");
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            m.updateLog(TAG+" gps BroadcastReceiver ","gps was turned on (or maybe off)");
            try {
                if(!mGoogleApiClient.isConnected())
                    stopSelf();

                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean gps = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean internet = m.isNetworkAvailable();

                m.updateLog(TAG+" gps BroadcastReceiver ","Tired! Sleeping for two seconds. I'm not sure why I'm sleeping but experiment with me");
                Thread.sleep(2000);

                // only call the code (getLastLocation, address and placesCode) when the gps and internet is turned on
                // I sleep a lot
                if(gps&&internet){
                    m.updateLog(TAG+" gps BroadcastReceiver ","Got some gps and internet");
                    if (mCurrentLocation==null){
                        m.updateLog(TAG+" gps BroadcastReceiver ","But goddamn location is null. Need to sleep.");
                        Thread.sleep(2000);
                        Thread.sleep(2000);
                        Thread.sleep(2000);
                        m.updateLog(TAG+" gps BroadcastReceiver ","6 seconds sleep ain't bad. Experiment with me. Ever thought of using asynctask");
                        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    }
                    if (mCurrentLocation!=null){
                        m.updateLog(TAG+" gps BroadcastReceiver ","location not null anymore. getting addresses");
                        addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                        m.updateLog(TAG+" gps BroadcastReceiver ","calling places code");
                        placesCode();
                    }
                }
                // register a broadcast receiver - for whenever the gps is turned on/off
                // we do some work when it's status is turned on
                getApplicationContext().unregisterReceiver(gpsReceiver);
            }
            catch(IOException | InterruptedException | SecurityException e) {
                e.printStackTrace();
                m.updateLog(TAG + " gpsReceiver"," Some Exceptions" +e.getMessage());
            }
        }
        }
    };

    public void onConnectionSuspended(int i) {
        m.calledMethodLog(TAG,"onConnectionSuspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        m.calledMethodLog(TAG,"onLocationChanged");
    }

    // this method gets the address and lets you make selection what parameters of address to include
    @SuppressLint("LongLogTag")
    private String getAddress() {
        m.calledMethodLog(TAG," GetAddress");
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();

        String address = addresses.get(0).getAddressLine(0)
                .replace(state,"").replaceFirst(country,"").replaceFirst(postalCode,"").replaceAll(",","").trim();

        m.updateLog(TAG+" Address",address);
//        String street = addresses.get(0).getFeatureName();
//        String city = addresses.get(0).getLocality();
//        String number = addresses.get(0).getFeatureName();
//        String council = addresses.get(0).getSubAdminArea();
//         null- getSubLocality(),getPremises(),getThoroughfare()
        return address;
    }

    // this code starts with building the places URL and then call the actual places code
    private void placesCode() {
        m.calledMethodLog(TAG,"placesCode");

        m.updateLog(TAG+"Places code "," Yeah! I'm the coolest method you've been looking for all this time. I'm a revolution!");
        String sb;
        try {
            m.updateLog(TAG+" places code","Building placed url. Wait.");
            sb = buildPlacesURL().toString();
            m.updateLog(TAG+" places code"," Done. Now calling placesTask code");
            new PlacesTask().execute(sb);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            m.updateLog(TAG + " places code"," Some Exceptions"+e.getMessage());
        }
    }
    // builds the url for fetching the nearby places
    @SuppressLint({"LongLogTag", "MissingPermission"})
    public StringBuilder buildPlacesURL() throws UnsupportedEncodingException {
        m.calledMethodLog(TAG,"buildPlacesURL");

        double mLatitude = mCurrentLocation.getLatitude();
        double mLongitude = mCurrentLocation.getLongitude();
        int mRadius = 500;

        String location = "https://www.google.com/maps/search/?api=1&query=" + mLatitude + "," + mLongitude;
        URL = location;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //calling a direct method which runs urlShortner code
        URL = m.urlShortner(location);

        String key = getApplicationContext().getString(R.string.API_KEY_GEO);

        if (mCurrentLocation==null) mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(!m.isNetworkAvailable()) return new StringBuilder(CONSTANT.NO_INTERNET);

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + mLatitude + "," + mLongitude);
        sb.append("&radius="+mRadius);
        sb.append("&types=" +  URLEncoder.encode("point_of_interest", "UTF-8"));
        sb.append("&sensor=true");
        sb.append("&key=" + key);
        m.updateLog(TAG+""+" PlacesURL", sb.toString());
        return sb;
    }

    // download the data from the URL
    private String downloadUrl(String strUrl) throws IOException{
        m.calledMethodLog(TAG,"downloadURL");

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
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            m.updateLog(TAG + " download url"," Some exception"+e.getMessage());
        } finally {
            if (iStream != null) iStream.close();
            if (urlConnection != null) urlConnection.disconnect();
        }
        return data;
    }

    //set the place name to the global variable
    public void setPlaceName(String s){
        m.calledMethodLog(TAG,"setPlaceName");
        placeName = s;
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
            List<HashMap<String, String>> places = new ArrayList<>();
            Place_JSON placeJson = new Place_JSON();
            try {
                jObject = new JSONObject(jsonData[0]);
                places = placeJson.parse(jObject);
            } catch (Exception e) {
                m.updateLog(TAG + " Parser task"," Some exception" +e.getMessage());
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
                m.updateLog(TAG+" parser task ","Places "+name);

                m.updateLog(TAG+" parser task ","Calling getAddress now");
                address = getAddress();
                m.updateLog(TAG+" parser task ","Sending broadcast now");
                sendBroadcast();
            }
        }
    }
    // gets the details of the place to let you choose which parameters of place you want to show
    public class Place_JSON {
         //Receives a JSONObject and returns a list
        public List<HashMap<String, String>> parse(JSONObject jObject) {
            JSONArray jPlaces = null;
            try {
                jPlaces = jObject.getJSONArray("results");
            } catch (JSONException e) {
                e.printStackTrace();
                m.updateLog(TAG + " Places JSON parse"," Some exception" +e.getMessage());
            }
            //Invoking getPlaces with the array of json object where each json object represent a place
            return getPlaces(jPlaces);
        }
        private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
            int placesCount = jPlaces.length();
            List<HashMap<String, String>> placesList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> place = null;
            //Taking each place, parses and adds to list object
            for (int i = 0; i < placesCount; i++) {
                try {
                    //Call getPlace with place JSON object to parse the place
                    place = getPlace((JSONObject) jPlaces.get(i));
                    placesList.add(place);
                } catch (JSONException e) {
                    e.printStackTrace();
                    m.updateLog(TAG + " Parser task getPlaces"," Some exception" +e.getMessage());
                }
            }
            return placesList;
        }
        private HashMap<String, String> getPlace(JSONObject jPlace){
            HashMap<String, String> place = new HashMap<String, String>();
            String placeName = "-NA-";
            String vicinity = "-NA-";
            String latitude;
            String longitude;
            String reference;
            String placeType;
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
                m.updateLog(TAG + " Parser task getPlaces"," Some exception" +e.getMessage());
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
                m.updateLog(TAG,e.getMessage());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask(context);
            parserTask.execute(result);
            m.updateLog(TAG+" "+" PlacesTask ","Hey! I'm places task. I've just called parsertask.");
        }
    }
    //sends the broadcast when the place name is fetched
    // the broadcast is registered in the class where the data is required
    private void sendBroadcast (){
        m.calledMethodLog(TAG,"sendBroadcast");

        m.updateLog(TAG,"Hey! This is sendBroadcast");

        Intent intent = new Intent ("message"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra(CONSTANT.ADDRESS,address);
        intent.putExtra(CONSTANT.PLACE_NAME,placeName);
        intent.putExtra(CONSTANT.URL_SHORTNER_SHARE_LOCATION,URL);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        m.updateLog(TAG,"I'm done. Yours sincerely SendBroadcast. P.S Don't miss me!");
    }
}