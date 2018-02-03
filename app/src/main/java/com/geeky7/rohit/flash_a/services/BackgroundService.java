/*
* BackgroundService runs when a SMS is received
* called by SMSReceiver BroadcastReceiver
* Checks for the keyword and if the service is enabled and then starts the locationService
* also adds in shared preferences if the sender is selected to send eta to
* also shows a notification if the internet is off when the location is requested i.e message is received
* */
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

import com.geeky7.rohit.flash_a.CONSTANT;
import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.MyApplication;

import java.util.HashSet;
import java.util.Set;

public class BackgroundService extends Service {
    public static final String TAG = CONSTANT.BACKGROUND_SERVICE;
    String message = "No text";
    String sender = "Empty";
    SharedPreferences preferences;
    Main m ;

    Set<String> selectedContacts = new HashSet<>();
    public BackgroundService() {
    }

    //sets up sharedPreferences
    @Override
    public void onCreate() {
        super.onCreate();
        m = new Main(MyApplication.getAppContext());
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    // calls the location service
    private void startService() {
        stopService(new Intent(this, LocationService.class));
        startService(new Intent(getApplicationContext(), LocationService.class));
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = null;
        SharedPreferences.Editor editor = preferences.edit();

        boolean service = preferences.getBoolean(CONSTANT.SERVICE,true);
        String keyword = preferences.getString(CONSTANT.KEYWORD,"Asha");

        if(intent!=null)
            extras = intent.getExtras();

        // if the bundle is not null; lets get some data from it or maybe all of it :)
        if (extras != null) {
            //getting message and sender# from the bundle received from SMSReceiver
            message = extras.getString("Message");
            // sender is sender#
            sender = extras.getString(CONSTANT.SENDER);

            selectedContacts = preferences.getStringSet(CONSTANT.SELECTED_CONTACTS,selectedContacts);

            //get the name of the sender from the number
            String senderName = m.getContactName(sender);

            boolean flag = false;
            // check if message received is from the selected contacts for sending eta
            // if found one just add it to shared preferences
            for (String selectedContact:selectedContacts){
                //m.updateLog(TAG, CONSTANT.SEND_ETA_IF_CONTACT_SELECTED + " All contacts "+ s);
                if (selectedContact.equals(senderName)){
                    //m.updateLog(TAG,CONSTANT.SEND_ETA_IF_CONTACT_SELECTED+" matched");
                    editor.putBoolean(CONSTANT.SEND_ETA_IF_CONTACT_SELECTED,true);
                    editor.apply();
                    flag = true;
                    break;
                }
            }
            // if the message is not from a person in the selected contacts list add the value in shared preference as false
            if (!flag) editor.putBoolean(CONSTANT.SEND_ETA_IF_CONTACT_SELECTED,false);
            editor.putString(CONSTANT.SENDER,sender);
            editor.apply();

            m.updateLog(TAG,"Message is:"+ message);

            //removing trailing spaces- when selected text from suggested words, a space at the last is automatically added
            keyword = keyword.trim();
            message = message.trim();
            boolean internet = m.isNetworkAvailable();
            // message is the keyword and service is enabled in the app then we are interested
            if (keyword.equals(message) && service){
                // if no internet show a notification to turn internet on
                // and register the network connected broadcast receiver
                if (!internet){
                    m.pugNotification("Location requested","No internet Connectivity.","Please turn on internet");
                    getApplicationContext().registerReceiver(internetReceiver,
                            new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                }
                // if internet is available then start locationService and stop background service
                else if (internet){
                    startService();
                    stopSelf();
                }
            }
        }
        return START_NOT_STICKY;
    }

    // runs when internet status is changed- that is turned on if it was off and vice versa
    // we are only interested if the internet is turned on now but was off when the location was requested
    private BroadcastReceiver internetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches("android.net.conn.CONNECTIVITY_CHANGE")) {
                // if internet is available start locationService
                // and stop backgroundService
                if (m.isNetworkAvailable()){
                    //Main.showToast("Internet status seems to be changed? Is it correct? Yes! Crazy");
                    startService();
                    stopSelf();
                }

            }
        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        m.updateLog(TAG,"onDestroy");
    }
}