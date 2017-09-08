package com.geeky7.rohit.flash_a.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class BackgroundService extends Service {
    String message = "No text";
    String sender = "Empty";
    SharedPreferences preferences;
    boolean locationPermission = true;
    public BackgroundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }
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
        if(intent!=null)
            extras = intent.getExtras();

        if (extras != null) {
            message = extras.getString("Message");
            sender = extras.getString("Sender");
            editor.putString("sender",sender);
            Log.i("Message","Message is:"+ message);

            if ("Where".equals(message) && locationPermission) {
                Log.i("Matched", "Location requested");
                startService();
                Log.i("LocationService", "Location Service initiated");
                stopSelf();
//              if(message.contains("Where")){
                // this message initiated the location service which fetches the location and converts into an address
            }
        }
        editor.commit();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("BackgroundService","onDestroy");
    }
}
