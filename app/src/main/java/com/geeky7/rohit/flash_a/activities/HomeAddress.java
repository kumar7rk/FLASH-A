package com.geeky7.rohit.flash_a.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.geeky7.rohit.flash_a.R;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;


public class HomeAddress extends AppCompatActivity {

//    AutoCompleteTextView homeAddress;
    FloatingActionButton floatingActionButton;
    private TextView mPlaceDetailsText;

    private TextView mPlaceAttribution;
    SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_address);
        mPlaceDetailsText = (TextView) findViewById(R.id.place_details);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);


        String homeAddress = preferences.getString("homeAddress",null);
        mPlaceDetailsText.setText(homeAddress);
        floatingActionButton = (FloatingActionButton)findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    openAutocompleteActivity();
            }
        });
    }

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
    }@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final SharedPreferences.Editor editor = preferences.edit();
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i("HomeAddress", "Place Selected: " + place.getName());

                mPlaceDetailsText.setText(place.getAddress());
                editor.putString("homeAddress",place.getAddress()+"");

                // Display attributions if required.
                CharSequence attributions = place.getAttributions();
                if (!TextUtils.isEmpty(attributions)) {
//                    mPlaceAttribution.setText(Html.fromHtml(attributions.toString()));
                } else {
                    mPlaceAttribution.setText("");
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e("HomeAddress", "Error: Status = " + status.toString());
            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
            }
        }
    }
}
