package com.geeky7.rohit.flash_a.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.R;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class HomeAddress extends AppCompatActivity implements OnMapReadyCallback {

    FloatingActionButton floatingActionButton;
    private TextView homeAddress;
    NestedScrollView scrollView;

    SharedPreferences preferences;

    private GoogleMap mMap;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_address_new);

        scrollView = (NestedScrollView)findViewById(R.id.nested);
        homeAddress = (TextView) findViewById(R.id.homeAddress_tv);
        floatingActionButton = (FloatingActionButton)findViewById(R.id.fab);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Setting the homeAddress in the textView
        String homeAddressS = preferences.getString("homeAddress",getResources().getString(R.string.home_address_text));
        homeAddress.setText(homeAddressS);

        //loading the map with a marker on the home address
        refreshMap();

        // to lock the scroll on the scroll view
        // you are still able to scroll from the mapView which is kind of cool
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                return true;
            }
        });

        // clicking fab open up the search bar powered by Google
        // Selecting one of the address updates the text view in the app and also the sharedPreference

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    openAutocompleteActivity();
            }
        });
    }

    // The method loads the map
    private void refreshMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // getting lat long from the home address
    // returns lat long which are used to put the marker on the map
    public LatLng getLocationFromAddress(String strAddress){

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );


        } catch (IOException e) {
            e.printStackTrace();
        }
            return p1;
    }

    // this open up the search bar to select the home addresss
    // this is more like an intermediate activity which closes once the address is selected
    // sample code Google
    private void openAutocompleteActivity() {
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(this);
            startActivityForResult(intent, 1);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

        }
    }
    // this returns the selected address from the activity and set the textView with the same
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final SharedPreferences.Editor editor = preferences.edit();
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i("HomeAddress", "Place Selected: " + place.getName());

                // set the current address in the textView
                homeAddress.setText(place.getAddress());
                Main.showToast("Home address updated :)");

                // when the home address is updated; this method dynamically loads the mapView
                refreshMap();

                editor.putString("homeAddress",place.getAddress()+"");
                editor.apply();
                // will test; if nothing goes wrong would delete this code the next time I read this comment
                /*// Display attributions if required.
                CharSequence attributions = place.getAttributions();
                if (!TextUtils.isEmpty(attributions)) {
                } else {
//                    mPlaceAttribution.setText("");
                }*/
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e("HomeAddress", "Error: Status = " + status.toString());
            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
            }
        }
    }

    // this is the overidden method which sets the marker and shoes the mapView along with the above method refreshMap
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String homeAddressS = preferences.getString("homeAddress",getResources().getString(R.string.home_address_text));
        if (homeAddressS.equals(getResources().getString(R.string.home_address_text))){

        }
        else{
        LatLng g = getLocationFromAddress(homeAddressS);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mMap.addMarker(new MarkerOptions().position(g));
        mMap.getCameraPosition();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(g, 13.0f));
        }
    }
}
