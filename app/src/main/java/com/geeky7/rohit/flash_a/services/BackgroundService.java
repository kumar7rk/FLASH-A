// this service runs when a SMS is received; called by SMSReceiver BroadcastReceiver
// Checks for the keywords and then starts the locationService

package com.geeky7.rohit.flash_a.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
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
        Bundle extras = null;
        SharedPreferences.Editor editor = preferences.edit();
        boolean service = preferences.getBoolean(CONSTANT.SERVICE,true);
        String keyword = preferences.getString(CONSTANT.KEYWORD,"Asha");
//        Log.i(TAG,service+"");
        if(intent!=null)
            extras = intent.getExtras();

        // if the bundle is not null; lets get some data from it or maybe all of it :)
        if (extras != null) {
            message = extras.getString("Message");
            sender = extras.getString("Sender");
            editor.putString("sender",sender);
            editor.apply();
            Log.i(TAG,"Message is:"+ message);

            //removing trailing spaces- when selected text from suggested words, a space at the last is automatically added
            keyword = keyword.trim();
            message = message.trim();

            // checks for the keyword, location permission, internet and if service is enabled from the app
            // if matched start location service

            boolean internet = m.isNetworkAvailable();
            if (keyword.equals(message) && !internet && service){
                m.pugNotification("Location requested","No internet Connectivity.","Please turn on internet");

                getApplicationContext().registerReceiver(internetReceiver,
                        new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            }
            if (keyword.equals(message) && internet && service){
                startService();
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    private BroadcastReceiver internetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches("android.net.conn.CONNECTIVITY_CHANGE")) {

                if (m.isNetworkAvailable()){
                    Main.showToast("Internet status seems to be changed? Is it correct? Yes! Crazy");
                    startService();
                    stopSelf();
                }

            }
        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
    }
}