package com.geeky7.rohit.flash_a;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.widget.Toast;

import br.com.goncalves.pugnotification.notification.PugNotification;

/**
 * Created by Rohit on 9/08/2016.
 */
public class Main {

    Context mContext;

    public Main(Context mContext) {
        this.mContext = mContext;
    }


    // This method pretty much works like the original method it is just that I don't have to pass all the parameters always
    // and if I were to suppress any toast message in the final product, I could just comment the code in the method
    // ez pz
    public static void showToast(String text) {
        Toast.makeText(MyApplication.getAppContext(), text, Toast.LENGTH_SHORT).show();
    }
    // checks if the internet is available on the device at the moment or not
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) MyApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    // creates notification using pugNotification library ez pz
    public void pugNotification(String title ,String message, String bigText){
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PugNotification.with(MyApplication.getAppContext())
                .load()
                .title(title)
                .message(message)
                .smallIcon(R.drawable.icon_notification)
                .bigTextStyle(message+" "+bigText)
                .largeIcon(R.drawable.icon_notification)
                .flags(android.app.Notification.DEFAULT_ALL)
                .simple()
                .build();
    }
}