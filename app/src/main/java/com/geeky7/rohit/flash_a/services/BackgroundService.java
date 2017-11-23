// this service runs when a SMS is received; called by SMSReceiver BroadcastReceiver
// Checks for the keywords and then starts the locationService

package com.geeky7.rohit.flash_a.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.geeky7.rohit.flash_a.CONSTANT;
import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.MyApplication;

public class BackgroundService extends Service {
    public static final String TAG = CONSTANT.BACKGROUND_SERVICE;
    String message = "No text";
    String sender = "Empty";
    SharedPreferences preferences;
    boolean locationPermission = true;
    Main m ;

    public BackgroundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        m = new Main(MyApplication.getAppContext());
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    // calls the location service
    private void startService() {
        Intent serviceIntent = new Intent(getApplicationContext(), LocationService.class);
        startService(serviceIntent);
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationPermission = preferences.getBoolean("locationPermission",false);
        Bundle extras = null;
        SharedPreferences.Editor editor = preferences.edit();
        boolean service = preferences.getBoolean(CONSTANT.SERVICE,true);
        String keyword = preferences.getString(CONSTANT.KEYWORD,"Asha");
        Log.i(TAG,service+"");
        if(intent!=null)
            extras = intent.getExtras();

        // if the bundle is not null; lets get some data from it or maybe all of it :)
        if (extras != null) {
            message = extras.getString("Message");
            sender = extras.getString("Sender");
            editor.putString("sender",sender);
            Log.i(TAG,"Message is:"+ message);

            // checks for the keyword, location permission, internet and if service is enabled from the app
            // if matched start location service
            if (keyword.equals(message) && locationPermission && m.isNetworkAvailable()&&service)
            {
//                Log.i(TAG, "Location requested");
                startService();
//                Log.i(TAG, "Location Service initiated");
                stopSelf();
            }
        }
        editor.apply();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
    }
}