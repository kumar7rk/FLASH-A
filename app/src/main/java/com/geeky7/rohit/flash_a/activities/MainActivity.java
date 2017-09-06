package com.geeky7.rohit.flash_a.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.geeky7.rohit.flash_a.R;
import com.geeky7.rohit.flash_a.services.LocationService;

public class MainActivity extends AppCompatActivity {

    String message = "No text";
    String sender = "Empty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        checkPermission();

        /*final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);*/

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            message = extras.getString("Message");
            sender = extras.getString("Sender");
            Log.i("Message","Message is:"+ message);

            if ("Where".equals(message)) {
//                Main.showToast("Location requested");
                Log.i("Matched", "Location requested");
                startService();
                Log.i("LocationService", "Location Service initiated");

//              if(message.contains("Where")){
                // this message initiated the location service which fetches the location and converts into an address
            }
        }

    }
    // from Google

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }
    public void checkPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_SMS},
                0);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                0);
    }
    private void startService() {
        Intent serviceIntent = new Intent(getApplicationContext(), LocationService.class);
        startService(serviceIntent);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
}
