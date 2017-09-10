package com.geeky7.rohit.flash_a.services;

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
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
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

import java.io.IOException;
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
//    static Context context;

    Main m;

    Geocoder geocoder;
    List<Address> addresses;

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
/*//        boolean b1 = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean b1 = manager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

        Log.i("b1",b1+"");
//        Log.i("b2",b2+"");
        if(!b&&b1){
            Log.i("b1","yes! Passive provider is being helpful");
            mCurrentLocation = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            try {
                addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                sendSMS();
                updateToastLog();
                stopSelf();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        // if location null, get last known location, updating the time so that we don't show quite old location
        if (mCurrentLocation==null){
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//            Log.i("LastLocation",mCurrentLocation.getLatitude()+", "+ mCurrentLocation.getLongitude());
            try {
                if (b){
                    addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                    sendSMS();
                    updateToastLog();
                    stopSelf();
                }
                else{
                    Log.i("Else", "gps off");
                    m.openLocationSettings(manager);
                    getApplicationContext().registerReceiver(gpsReceiver,
                            new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }
        /*if (mRequestingLocationUpdates)
            startLocationupdates();*/

//        googleApiClientConnected = true;
    }
    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                //Do your stuff on GPS status change

//                Log.i("onReceive","Building api client");
//                buildGoogleApiClient();
                /*if(!mGoogleApiClient.isConnected()){
                    Log.i("onReceive","Not connected");
                    mGoogleApiClient.connect();
                }
                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                */
                Log.i("onReceive",mGoogleApiClient.isConnected()+"");
                // googleAPI is connected and ready to get location updates- start fetching current location
                try {
                    if(!mGoogleApiClient.isConnected()){
                        stopSelf();
//                        abortBroadcast();
//                        Thread.sleep(12000);
                        Log.i("onReceive","Google api client not connected. Mission abort");
                        /*Log.i("onReceive","mGoogleApiClient.isConnected, starting location updates");
                        startLocationupdates();
                        Log.i("onReceive","Started location update");*/
                    }
                    Thread.sleep(2000);
                    if (mCurrentLocation==null){
                        Log.i("onReceive","Current location null. haha!");
                        Thread.sleep(2000);
//                        startLocationupdates();
                        Thread.sleep(2000);
                        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//                        Thread.sleep(2000);
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
                    stopSelf();
                    sendSMS();
                    Log.i("onReceive","Mission accomplished. You have done it man.");
                    updateToastLog();
//                    gpsReceiver.abortBroadcast();
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
        //Main.showToast("I'm called- onLocationChanged");
        /*mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        try {
            addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateToastLog();*/
    }
    private void sendSMS() {
        String address = setAddress();
        SmsManager manager = SmsManager.getDefault();
        manager.sendTextMessage(sender,null, address, null, null);
        pugNotification("Location shared","Your current location shared with ",sender);
    }
    private String setAddress() {
        for (int i = 0; i< addresses.size();i++)
            Log.i("All addresses",addresses.get(i).getAddressLine(i));

        String address = addresses.get(0).getAddressLine(0);

//        String city = addresses.get(0).getLocality();
//        String state = addresses.get(0).getAdminArea();
//        String country = addresses.get(0).getCountryName();
//        String postalCode = addresses.get(0).getPostalCode();
//        String knownName = addresses.get(0).getFeatureName();

        return address;
    }
    public void pugNotification(String title ,String message, String bigText){
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
}

