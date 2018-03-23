package com.geeky7.rohit.flash_a.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.geeky7.rohit.flash_a.CONSTANT;
import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
public class CurrentLocationMapActivity extends AppCompatActivity implements
        OnMapReadyCallback
        , GoogleMap.OnMarkerClickListener{

    Main m;
    private GoogleMap mMap;
    private static final String TAG = CONSTANT.CURRENT_LOCATION_MAP_ACTIVITY;

    private String address, latitude, longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        address = intent.getStringExtra(CONSTANT.ADDRESS);
        latitude = intent.getStringExtra(CONSTANT.LATITUDE);
        longitude = intent.getStringExtra(CONSTANT.LONGITUDE);

        super.onCreate(savedInstanceState);
        m = new Main(getApplicationContext());
        setContentView(R.layout.activity_current_location_map);
        setTitle(address);
        loadMap();
    }

    private void loadMap() {
        m.calledMethodLog(TAG,"loadMap");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        m.calledMethodLog(TAG,"onMapReady");
        mMap = googleMap;
        double lat = Double.parseDouble(latitude);
        double lng = Double.parseDouble(longitude);
        LatLng g = new LatLng(lat, lng);

        mMap.addMarker(new MarkerOptions().position(g));
        //mMap.getCameraPosition();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(g, 15.0f));

        mMap.setOnMarkerClickListener(this);

    }

    @Override
    public void onBackPressed() {
        m.calledMethodLog(TAG,"onBackPressed");


        super.onBackPressed();
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setTitle(address);
        return false;
    }
}
