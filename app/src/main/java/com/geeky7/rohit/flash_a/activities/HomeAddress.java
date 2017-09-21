package com.geeky7.rohit.flash_a.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.geeky7.rohit.flash_a.R;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

public class HomeAddress extends AppCompatActivity {

    AutoCompleteTextView homeAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_address);
//        homeAddress = (AutoCompleteTextView)findViewById(R.id.homeAddress);
        try {
            openAutoComplete();
        } catch (GooglePlayServicesNotAvailableException e) {
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);
            Log.e("HomeAddress", message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        }

    }
    public void openAutoComplete() throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {

        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                .build(this);
        startActivityForResult(intent, 1);
    }
}
