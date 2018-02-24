package com.geeky7.rohit.flash_a.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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

import java.io.IOException;
import java.util.List;

public class CurrentLocationMapActivity extends AppCompatActivity implements
        OnMapReadyCallback
        , GoogleMap.OnMarkerClickListener{

    Main m;
    private GoogleMap mMap;
    private static final String TAG = CONSTANT.CURRENT_LOCATION_MAP_ACTIVITY;

    private String address;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        address = intent.getStringExtra(CONSTANT.ADDRESS);
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
        LatLng g = getLocationFromAddress(address);

        mMap.addMarker(new MarkerOptions().position(g));
        //mMap.getCameraPosition();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(g, 15.0f));

        mMap.setOnMarkerClickListener(this);

    }
    // getting lat long from the home address
    // returns lat long which are used to put the marker on the map
    public LatLng getLocationFromAddress(String strAddress){
        m.calledMethodLog(TAG,"getLocationFromAddress");
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng p1 = null;
        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) return null;
            Address location=address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p1;
    }

    @Override
    public void onBackPressed() {
        m.calledMethodLog(TAG,"onBackpressed");
        super.onBackPressed();
    }
    @Override
    public boolean onMarkerClick(Marker marker) {

        marker.setTag(address);
        marker.setTitle(address);
        return false;
    }
}
