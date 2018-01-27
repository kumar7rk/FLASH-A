package com.geeky7.rohit.flash_a;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.geeky7.rohit.flash_a.activities.Design;
import com.geeky7.rohit.flash_a.services.LocationService;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.urlshortener.Urlshortener;

import java.io.IOException;

import br.com.goncalves.pugnotification.notification.PugNotification;

/**
 * Created by Rohit on 9/08/2016.
 */
public class Main {

    Context mContext;

    public Activity activity;
    public Main(Context mContext) {
        this.mContext = mContext;
    }

    public Main(Activity _activity){
        this.activity = _activity;
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
                .click(Design.class,null)
                .simple()
                .build();
    }

    public String getContactName(final String phoneNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName = "";
        Cursor cursor = MyApplication.getAppContext().getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
                cursor.close();
            }
        }
        return contactName;
    }

    public String urlShortner(String longUrl){
        Urlshortener.Builder builder = new Urlshortener.Builder (AndroidHttp.newCompatibleTransport(),
                AndroidJsonFactory.getDefaultInstance(), null);
        Urlshortener urlshortener = builder.build();

        com.google.api.services.urlshortener.model.Url url = new com.google.api.services.urlshortener.model.Url();
        url.setLongUrl(longUrl);
        try {
            Urlshortener.Url.Insert insert=urlshortener.url().insert(url);
            insert.setKey(MyApplication.getAppContext().getResources().getString(R.string.API_KEY_URL));
            url = insert.execute();
            return url.getId();
        } catch (IOException e) {
            Log.e(LocationService.TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    public void updateLog(String className, String text){
        Log.i(className,text);
    }

    // indefinite with a button
    public void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(activity.findViewById(android.R.id.content),
                activity.getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(activity.getString(actionStringId), listener).show();
    }
}