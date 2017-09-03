package com.geeky7.rohit.flash_a.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.R;

public class MainActivity extends AppCompatActivity {

    String message = "";
    String sender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        boolean enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            message = extras.getString("Message");
            sender = extras.getString("Sender");
            Log.i("Text","Text is:"+ message);

//            if ("Where".equals(message)) {
              if(message.contains("Where")){
                Main.showToast("Location requested");
                // this message initiated the location service which fetches the location and converts into an address
                startService();
            }
        }

    }
    public void checkPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                0);
    }
    private void startService() {
        Intent serviceIntent = new Intent(getApplicationContext(), LocationService.class);
        startService(serviceIntent);
    }
}
