// this code is to get the context of the app which could be later given to any code, be it be an onclickListener
// or anything where it is virtually impossible to get the context
// I still remember when I discovered this code it was a revolution :)
// Disclamer : if I insert :D instead of :), it either I am being lazy to reach parantheses
// or I am just too happy. Mainly it's the former

package com.geeky7.rohit.flash_a;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

/**
 * Created by Rohit on 25/08/2016.
 */
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}