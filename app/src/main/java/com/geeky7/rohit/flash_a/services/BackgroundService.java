package com.geeky7.rohit.flash_a.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class BackgroundService extends Service {
    String message = "No text";
    String sender = "Empty";
    public BackgroundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
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
        Bundle extras = intent.getExtras();
        if (extras != null) {
            message = extras.getString("Message");
            sender = extras.getString("Sender");
            Log.i("Message","Message is:"+ message);

            if ("Where".equals(message)) {
                Log.i("Matched", "Location requested");
                startService();
                Log.i("LocationService", "Location Service initiated");
                stopSelf();
//              if(message.contains("Where")){
                // this message initiated the location service which fetches the location and converts into an address
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
