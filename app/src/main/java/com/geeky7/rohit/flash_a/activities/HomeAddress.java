/*
Class to play around with the home address
update home address, delete, shows a mapView

Everything packed up in cheesy classic design
*/

package com.geeky7.rohit.flash_a.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.geeky7.rohit.flash_a.CONSTANT;
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

    private static final String TAG = CONSTANT.HOME_ADDRESS;

    FloatingActionButton floatingActionButton;
    private TextView homeAddress;
    NestedScrollView scrollView;
    private ImageView delete;
    private GoogleMap mMap;

    public Main m;
    SharedPreferences preferences;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        m = new Main(this);
        m.calledMethodLog(TAG,"onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_address);

        // find the views ;of all the elements in the xml
        findViewByIds();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        //fetching saved homeAddress from sharedPreferences
        final String homeAddressS = preferences.getString(CONSTANT.HOME_ADDRESS,getResources().getString(R.string.home_address_text));
        // Setting the homeAddress in the textView
        homeAddress.setText(homeAddressS);

        // hides the delete button when there is no homeAddress
        if (homeAddressS.equals(getResources().getString(R.string.home_address_text))){
            delete.setVisibility(View.INVISIBLE);
            homeAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openAutocompleteActivity();
                }
            });
        }
        //updates the map with a marker on the home address
        refreshMap();

        // to lock the scroll on the scroll view
        // you are still able to scroll from the mapView which is kind of cool--> nah blocked as well
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                return true;
            }
        });

        // clicking fab open up the search bar powered by Google
        // Selecting one of the address updates the text view in the app and also the sharedPreferences
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    openAutocompleteActivity();
            }
        });

        // delete icon which removes the address from the layout as well as SharedPreferences
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeAddress.setText(getResources().getString(R.string.home_address_text));
                editor.putString(CONSTANT.HOME_ADDRESS,homeAddress.getText().toString());
                editor.apply();
                delete.setVisibility(View.INVISIBLE);
            }
        });
    }

    //Find view by ids
    private void findViewByIds() {
        m.calledMethodLog(TAG,"findViewByIds");
        scrollView = (NestedScrollView)findViewById(R.id.nested);
        homeAddress = (TextView) findViewById(R.id.homeAddress_tv);
        floatingActionButton = (FloatingActionButton)findViewById(R.id.fab);
        delete = (ImageView) findViewById(R.id.delete_iv);
    }

    // The method loads the map
    // called onCreate and when the homeAddress is updated
    // this method calls onMapReady method- an overridden method
    private void refreshMap() {
        m.calledMethodLog(TAG,"refreshMap");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
    // this open up the search bar to select the home address
    // this is more like an intermediate activity which closes once the address is selected
    // sample code Google
    private void openAutocompleteActivity() {
        m.calledMethodLog(TAG,"openAutocompleteActivity");
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
        m.calledMethodLog(TAG,"onActivityResult");
        final SharedPreferences.Editor editor = preferences.edit();
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);
                m.updateLog(TAG, "Place Selected: " + place.getName());

                // set the current address in the textView
                homeAddress.setText(place.getAddress());

                delete.setVisibility(View.VISIBLE);
                // when the home address is updated; this method dynamically loads the mapView
                refreshMap();

                Main.showToast("Home address updated :)");

                editor.putString(CONSTANT.HOME_ADDRESS,place.getAddress()+"");
                editor.apply();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e(TAG, "Error: Status = " + status.toString());
            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
            }
        }
    }

    // this is the overridden method which sets the marker and shoes the mapView along with the above method refreshMap
    @Override
    public void onMapReady(GoogleMap googleMap) {
        m.calledMethodLog(TAG,"onMapReady");
        mMap = googleMap;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String homeAddressS = preferences.getString(CONSTANT.HOME_ADDRESS,getResources().getString(R.string.home_address_text));
        // if home address is not set
        if (homeAddressS.equals(getResources().getString(R.string.home_address_text))||homeAddressS.equals("")){
        }

        // We've a homeAddress. Let's go to person's place and say hello!!!!!
        else{
            Main m = new Main(getApplicationContext());
            m.updateLog(CONSTANT.HOME_ADDRESS,homeAddressS);

            //checking if internet is available because showing marker requires internet

            //Bug--> causes force close if activity is started with no internet access
            // founded by monkey yeaaahh!!! Been there done that (don't think about this too hard, alright, you gonna hurt yourself)

            if(m.isNetworkAvailable()){
                LatLng g = getLocationFromAddress(homeAddressS);
                if (null==g){
                    Main.showToast("Error loading map.");
                    m.updateLog(TAG,"Error loading map.");
                    return;
                }
                mMap.addMarker(new MarkerOptions().position(g));
                mMap.getCameraPosition();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(g, 13.0f));
            }
            // if no internet is available shows a Snackbar with a button to retry
            // it basically contains the same logic as above
            else{
                showSnackbar(R.string.map_no_internet_home_address, R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String homeAddressS = preferences.getString(CONSTANT.HOME_ADDRESS,getResources().getString(R.string.home_address_text));
                        Main m = new Main(getApplicationContext());
                        LatLng g = null;
                        if(m.isNetworkAvailable()) g = getLocationFromAddress(homeAddressS);
                        m.updateLog(TAG,g+ " value");
                        if (null==g){
                            Main.showToast("Error loading map.");
                            m.updateLog(TAG,"Error loading map.");
                            return;
                        }
                        mMap.addMarker(new MarkerOptions().position(g));
                        mMap.getCameraPosition();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(g, 13.0f));
                    }
                });
            }
        }
    }
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        m.calledMethodLog(TAG,"showSnackbar");
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }
    @Override
    public void onBackPressed() {
        m.calledMethodLog(TAG,"onBackpressed");
        super.onBackPressed();
    }
}