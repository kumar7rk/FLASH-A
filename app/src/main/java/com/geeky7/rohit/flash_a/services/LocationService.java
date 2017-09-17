package com.geeky7.rohit.flash_a.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;

import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
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

import br.com.goncalves.pugnotification.notification.PugNotification;


public class LocationService extends Service implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,LocationListener{

    public static final String TAG = "LocationService";
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

    String string = "";
    SharedPreferences preferences;
    String sender = "";
    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        sender = preferences.getString("sender","");
        m = new Main(getApplicationContext());
//        mLastUpdateTime = "";

        buildGoogleApiClient();
        mGoogleApiClient.connect();

        // googleAPI is connected and ready to get location updates- start fetching current location
        if(mGoogleApiClient.isConnected()/*&&mRequestingLocationUpdates*/)
            startLocationupdates();

        // for getting address

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
        Log.i("LocationService","onDestroy");
      //  Main.showToast("BackgroundServiceDestroyed");
        stopSelf();
        if (mGoogleApiClient.isConnected())
            stopLocationupdates();
        mGoogleApiClient.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
        //return START_STICKY;
    }

    // add the API and builds a client
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(LocationService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
        createLocationRequest();
    }

    // method- fetch location every `UPDATE_INTERVAL_IN_MILLISECONDS` milliseconds
    private void createLocationRequest() {
        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mlocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    // method- update the new coordinates
    protected void updateToastLog(){
        Log.i(TAG, mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());
        Log.i(TAG,setAddress());
    }
    // fetch location now
    protected void startLocationupdates() throws SecurityException {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mlocationRequest, this);
    }
    // location update no longer needed;
    protected void stopLocationupdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnected(Bundle bundle)throws SecurityException {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean b = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // if location null, get last known location, updating the time so that we don't show quite old location
        if (mCurrentLocation==null){
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            try {
                if (b){
                    addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                    String sb = null;
                    try {
                        sb = buildPlacesURL().toString();
                        new PlacesTask().execute(sb);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
//                    Thread.sleep(5000);
//                    sendSMS();
//                    updateToastLog();
                    stopSelf();
                }
                else{
                    Log.i("Else", "gps off");
                    String name = getContactName(sender,getApplicationContext());
                    pugNotification("Location Request from "+name,"Turn GPS on","");
                    getApplicationContext().registerReceiver(gpsReceiver,
                            new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }
    }
    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                //Do your stuff on GPS status change
                Log.i("onReceive",mGoogleApiClient.isConnected()+"");
                try {
                    if(!mGoogleApiClient.isConnected()){
                        stopSelf();
                        Log.i("onReceive","Google api client not connected. Mission abort");
                    }
                    Thread.sleep(2000);
                    if (mCurrentLocation==null){
                        Log.i("onReceive","Current location null. haha!");
                        Thread.sleep(2000);
                        Thread.sleep(2000);
                        Thread.sleep(2000);
                        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        Log.i("onReceive",mCurrentLocation.getProvider());
                        Log.i("onReceive",mCurrentLocation.getAccuracy()+"");
                        Log.i("onReceive",mCurrentLocation.getLatitude()+", "+mCurrentLocation.getLongitude());
                    }
                    if (mCurrentLocation!=null) {
                        Log.i("onReceive","Don't worry, its not null anymore");
                        addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                        Log.i("onReceive","Value for addresses list added");
                    }

                    Log.i("onReceive","Sending message now");
                    String sb = null;
                    try {
                        sb = buildPlacesURL().toString();
                        new PlacesTask().execute(sb);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    stopSelf();
                    Log.i("onReceive","Mission accomplished. You have done it man.");
                    updateToastLog();
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
    //check is user wants to monitor walking, if yes then listen to the recognised activity;
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
    }
    private void sendSMS(String s) {
        String name = getContactName(sender,getApplicationContext());
        String address = setAddress();
        SmsManager manager = SmsManager.getDefault();
        String string1 = "I am near "+ s+ ". "+ address;
//        manager.sendTextMessage(sender,null, address, null, null);
        manager.sendTextMessage(sender,null, string1, null, null);
        boolean noti = preferences.getBoolean("notification",true);

        if (noti)
            pugNotification("Location shared","Your current location shared with",name);
    }
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

    private String setAddress() {
        for (int i = 0; i< addresses.size();i++)
            Log.i("All addresses",addresses.get(i).getAddressLine(i));

        String address = addresses.get(0).getAddressLine(0);

//        String city = addresses.get(0).getLocality();
//        String state = addresses.get(0).getAdminArea();
//        String country = addresses.get(0).getCountryName();
//        String postalCode = addresses.get(0).getPostalCode();
//        String knownName = addresses.get(0).getFeatureName(); // unit 32

//        String s1 = addresses.get(0).getSubLocality(); // null
//        String s2 = addresses.get(0).getPremises(); // null
//        String s3 = addresses.get(0).getThoroughfare(); //null
//        String s4 = addresses.get(0).getSubAdminArea(); city of west torrens
//        return s1+ " " + s2+ " " + s3+ " "+ s4;
        return address;
    }
    public void pugNotification(String title ,String message, String bigText){
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PugNotification.with(getApplicationContext())
                .load()
                .title(title)
                .message(message)
                .smallIcon(R.drawable.quantum_ic_stop_white_24)
                .bigTextStyle(message+" "+bigText)
                .largeIcon(R.drawable.cast_ic_notification_small_icon)
                .flags(android.app.Notification.DEFAULT_ALL)
                .simple()
                .build();
    }
    public StringBuilder buildPlacesURL() throws UnsupportedEncodingException {
        double mLatitude = -34.923792;
        double mLongitude = 138.6047722;
        int mRadius = 500;

        mLatitude = mCurrentLocation.getLatitude();
        mLongitude = mCurrentLocation.getLongitude();

        String number1 = "AIzaSyCth6KThdK_C9mztGc2dadvK82yCvktO-o";

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + mLatitude + "," + mLongitude);
        sb.append("&radius="+mRadius);
        sb.append("&types=" +  URLEncoder.encode("point_of_interest", "UTF-8"));
        sb.append("&sensor=true");
        sb.append("&key=" + number1);
        Log.i("Places", sb.toString());
        return sb;
    }

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

            if (list.size() >0){
                HashMap<String, String> hmPlace = list.get(0);
                String name = hmPlace.get("place_name");
                string = name;
                sendSMS(name);
            }
        }
    }// onPostExecute
//}// end of the parserTask class
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

                Log.i("PlaceType",placeType);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return place;
        }
    }
    public class PlacesTask extends AsyncTask<String, Integer, String> {
        String data = null;
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.i("Background Task", e.toString());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask(context);
            parserTask.execute(result);
            Log.i("PlacesTaskOnPostExecute", result + "");
        }
    }
}


