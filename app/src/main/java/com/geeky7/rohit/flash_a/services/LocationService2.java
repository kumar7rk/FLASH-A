// This class gets the location convert it into address and find a landmark
// code only used by the action bar button- current location
// class is called when the gps and internet is on and the app is opened

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
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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

    public LocationService2() {
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

        Log.i(TAG,"LocationService2 Created");
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
                // if location null, get last known location, updating the time so that we don't show quite old location
                if (mCurrentLocation==null)
                    mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
//                initiates places code to fetch the name of the nearby place
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                placesCode();
                stopSelf();
            }
            // when gps if off registers a receiver listening for status of the gps to change
            else{
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
            //Do your stuff on GPS status change
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            try {
                if(!mGoogleApiClient.isConnected())
                    stopSelf();

                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean gps = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean internet = m.isNetworkAvailable();

                Thread.sleep(2000);

                // only call the code when the gps and internet is turned on
                if(gps&&internet){
                    if (mCurrentLocation==null){
                        Thread.sleep(2000);
                        Thread.sleep(2000);
                        Thread.sleep(2000);
                        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    }
                    if (mCurrentLocation!=null){
                        addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                        placesCode();

                    }
                }
                stopSelf();
                // register a broadcast receiver - for whenever the gos is turned on/off
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

    // this method gets the address and lets you make selection what parameters of address to include
    @SuppressLint("LongLogTag")
    private String getAddress() {
//        for (int i = 0; i< addresses.size();i++)
//            Log.i(TAG+""+" AllAddresses",addresses.get(i).getAddressLine(i));


        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();

        String address = addresses.get(0).getAddressLine(0)
                .replace(state,"").replaceFirst(country,"").replaceFirst(postalCode,"").replaceAll(",","").trim();

        Log.i(TAG+" Address",address);
//        String street = addresses.get(0).getFeatureName();
//        String city = addresses.get(0).getLocality();
//        String number = addresses.get(0).getFeatureName();
//        String council = addresses.get(0).getSubAdminArea();
//         null- getSubLocality(),getPremises(),getThoroughfare()
        return address;
    }

    // this code starts with building the places URL and then call the actual places code
    private void placesCode() {
//        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        boolean b = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        String sb = null;
        try {
//            if(b){
                sb = buildPlacesURL().toString();
                new PlacesTask().execute(sb);
//            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    // builds the url for fetching the nearby places
    @SuppressLint("LongLogTag")
    public StringBuilder buildPlacesURL() throws UnsupportedEncodingException {
        double mLatitude = 0;
        double mLongitude = 0;
        int mRadius = 500;
        String number1 = getApplicationContext().getString(R.string.API_KEY);

        if (mCurrentLocation==null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if(!m.isNetworkAvailable()) return new StringBuilder(CONSTANT.NO_INTERNET);

        mLatitude = mCurrentLocation.getLatitude();
        mLongitude = mCurrentLocation.getLongitude();
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + mLatitude + "," + mLongitude);
        sb.append("&radius="+mRadius);
        sb.append("&types=" +  URLEncoder.encode("point_of_interest", "UTF-8"));
        sb.append("&sensor=true");
        sb.append("&key=" + number1);
        Log.i(TAG+""+" PlacesURL", sb.toString());
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

    //set the place name to the global variable
    public void setPlaceName(String s){
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
//            List<HashMap<String, String>> places = null;
            List<HashMap<String, String>> places = new ArrayList<>();
            Place_JSON placeJson = new Place_JSON();
            try {
                jObject = new JSONObject(jsonData[0]);
                places = placeJson.parse(jObject);
            } catch (Exception e) {
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
                updateLog("Places "+name);
                address = getAddress();

                sendBroadcast();
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
            String latitude;
            String longitude;
            String reference;
            String placeType;

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
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask(context);
            parserTask.execute(result);
        }
    }

    //sends the broadcast when the place name is fetched
    // the broadcast is registered in the class where the data is required
    private void sendBroadcast (){
//        String address = getAddress();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent ("message"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra(CONSTANT.ADDRESS,address);
        intent.putExtra(CONSTANT.PLACE_NAME,placeName);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // method to show toast during testing and then comment the toast code in the production code
    public void updateLog (String s){
        Log.i(TAG,s);
//        Main.showToast(s);
    }
}