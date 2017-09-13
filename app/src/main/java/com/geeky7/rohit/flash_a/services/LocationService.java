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

        // if location null, get last known location, updating the time so that we don't show quite old location
        if (mCurrentLocation==null){
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            try {
                if (b){
                    addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                    sendSMS();
                    updateToastLog();
                    stopSelf();
                }
                else{
                    Log.i("Else", "gps off");
                    String name = getContactName(sender,getApplicationContext());
                    pugNotification("Location Request from "+name," has requested your location","Click this notification to turn location on");
                    getApplicationContext().registerReceiver(gpsReceiver,
                            new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
                }
            } catch (IOException e) {
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
                    stopSelf();
                    sendSMS();
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
    private void sendSMS() {
        String name = getContactName(sender,getApplicationContext());
        String address = setAddress();
        SmsManager manager = SmsManager.getDefault();
        manager.sendTextMessage(sender,null, address, null, null);
        boolean noti = preferences.getBoolean("notification",true);

        if (noti)
            pugNotification("Location shared","Your current location shared with",name);
    }
    public String getContactName(final String phoneNumber,Context context)
    {
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName="";
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            cursor.close();
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
//        String knownName = addresses.get(0).getFeatureName();

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
                .click(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), new Bundle())
                .build();
    }
}

