package com.geeky7.rohit.flash_a.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geeky7.rohit.flash_a.BuildConfig;
import com.geeky7.rohit.flash_a.CONSTANT;
import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class Design extends AppCompatActivity {

    //    private static final String TAG = Design.class.getSimpleName();
    private static final String TAG = CONSTANT.DESIGN;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int GPS_REQUEST_CODE = 42;
    private static final int CONTACT_REQUEST_CODE = 50;

    boolean locationPermission = true;
    boolean contactPermission = true;
    boolean SMSPermission = true;

    LinearLayout serviceEnabled_lay, homeAddress_lay, keyword_lay, customiseMessage_lay, history_lay, tutorial_lay;
    TextView serviceEnabled_tv;
    ImageView serviceEnabled_iv;

    SharedPreferences preferences;

    String add = "Could not fetch location. Retry";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.design);

        // checking if the permissions are not granted call request method which starts the procedure

        if (!checkPermissions())
            requestPermissions();

        // finding view by id for all the associated views
        findViewById();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean service = preferences.getBoolean("service", true);

        // fetching the service status from the sharedPreference and checking if its enabled or not
        // calling respective methods

        if (service) enableService();
        else disableService();

        // open HomeAddress activity when home address layout is clicked
        homeAddress_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), HomeAddress.class));
            }
        });

        keyword_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Keyword.class));
            }
        });
    }

    // find view by id of all the views
    private void findViewById() {
        serviceEnabled_tv = (TextView) findViewById(R.id.serviceEnabled_tv);
        serviceEnabled_iv = (ImageView) findViewById(R.id.serviceEnabled_iv);

        serviceEnabled_lay = (LinearLayout) findViewById(R.id.serviceEnabled_lay);
        homeAddress_lay = (LinearLayout) findViewById(R.id.homeAddress_lay);
        keyword_lay = (LinearLayout) findViewById(R.id.keyword_lay);
        customiseMessage_lay = (LinearLayout) findViewById(R.id.customiseMessage_lay);
        history_lay = (LinearLayout) findViewById(R.id.history_lay);
        tutorial_lay = (LinearLayout) findViewById(R.id.tutorial_lay);
    }

    //if the service is running and user hits the layout
    // this method runs and it changes the disables the service and changes the color of the layout to red
    public void disableService() {
        serviceEnabled_lay.setBackgroundColor(Color.RED);
        serviceEnabled_tv.setText(R.string.service_disabled);
        serviceEnabled_iv.setImageBitmap(null);
        serviceEnabled_iv.setImageResource(R.drawable.service_disabled);
        serviceEnabled_iv.setScaleType(ImageView.ScaleType.FIT_XY);
        serviceEnabled_tv.setPadding(20, 20, 20, 20);
    }

    // if the service is not running this method is called
    // changes the color to orange or something for some reasons
    public void enableService() {
        serviceEnabled_lay.setBackgroundColor(0xffff8800);
        serviceEnabled_tv.setText(R.string.service_enabled);
        serviceEnabled_iv.setImageBitmap(null);
        serviceEnabled_iv.setImageResource(R.drawable.service_enabled);
        serviceEnabled_iv.setScaleType(ImageView.ScaleType.FIT_XY);
        serviceEnabled_tv.setPadding(20, 20, 20, 20);
    }

    // this method checks if all the required permission are granted
    // returns a boolean
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionState1 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS);
        int permissionState2 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);

        return permissionState == PackageManager.PERMISSION_GRANTED &&
                permissionState1 == PackageManager.PERMISSION_GRANTED &&
                permissionState2 == PackageManager.PERMISSION_GRANTED;
    }

    // this method starts the location request
    // this method is called when the user has denied the permission once but has not checked Never ask againg checkbox
    private void requestPermissions() {
        boolean shouldProvideRationaleLocation =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);
        boolean shouldProvideRationaleSMS =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_SMS);
        boolean shouldProvideRationaleContact =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.

        // if contact permission is not granted/ denied last time
        if (shouldProvideRationaleContact) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale_contact, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startPermissionRequest();
                        }
                    });
        }
        // if either location or SMS permission is not granted; request
        if (shouldProvideRationaleLocation || shouldProvideRationaleSMS) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startPermissionRequest();
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startPermissionRequest();
        }
    }

    // just to show to user why this permission is required
    private void showSnackbar(final String text) {
        View container = findViewById(R.id.main_activity_container);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    // This method is called the first time the app is installed
    // request all the permissions stated here
    private void startPermissionRequest() {
        ActivityCompat.requestPermissions(Design.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_CONTACTS},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    // this is the important bit which checks if the permission is granted or not
    // and therefore change your functionality accordingly
    // this method sets the values of the boolean variable for location, contact and sms and store them in sharedPreference
    // although it is not the right practice because if a user revokes the permission then these variables are not updated
    // and can therefore cause crash
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        SharedPreferences.Editor editor = preferences.edit();
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                locationPermission = true;
                contactPermission = true;
                SMSPermission = true;
            }
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermission = true;
            }
            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                SMSPermission = true;
            }
            if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                contactPermission = true;
            }
            if (grantResults[0] == PackageManager.PERMISSION_DENIED ||
                    grantResults[1] == PackageManager.PERMISSION_DENIED ||
                    grantResults[2] == PackageManager.PERMISSION_DENIED) {
                // Permission denied.
                locationPermission = false;
                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
        editor.putBoolean("locationPermission", locationPermission);
        editor.putBoolean("SMSPermission", SMSPermission);
        editor.putBoolean("contactPermission", contactPermission);
        editor.apply();
    }

    // just show a snackbar
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    // would respond to the onClick listeners on layout
    @Override
    protected void onResume() {
        super.onResume();

//        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("message"));

        final SharedPreferences.Editor editor = preferences.edit();
        serviceEnabled_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean service = preferences.getBoolean("service", true);
                if (service) {
                    Main.showToast("FL-ASHA disabled");
                    editor.putBoolean("service", false);
                    editor.apply();
                    disableService();
                } else {
                    Main.showToast("FL-ASHA enabled");
                    editor.putBoolean("service", true);
                    editor.apply();
                    enableService();
                }
            }
        });

        keyword_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Main.showToast(getResources().getString(R.string.coming_soon));
            }
        });

        customiseMessage_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Main.showToast(getResources().getString(R.string.coming_soon));
            }
        });

        history_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Main.showToast(getResources().getString(R.string.coming_soon));
            }
        });

        tutorial_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), TutorialActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_current_location:
                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean b = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                // if the gps is off
                if (!b) {
                    // calls this method which open the dialog which ask to enable location
                    // and enables the location when the user clicks ok
                    displayLocationSettingsRequest(getApplicationContext());
                } else {
                    buildDialogCurrentLocation();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void buildDialogCurrentLocation() {
//        startService(new Intent(this, 3.class));
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setTitle("Your Current Location")
            .setMessage(add)
            .setPositiveButton(getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .setNegativeButton(getResources().getString(R.string.share), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_PICK,  ContactsContract.Contacts.CONTENT_URI);
                    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(intent, CONTACT_REQUEST_CODE);
                    Main.showToast("Sharing location, select contact");
                }
            })
            .setIcon(android.R.drawable.ic_menu_mylocation)
            .show();
    }

    // onClick currentLocation button in actionBar and gps is off
    // opens a dialog which turns on gps without requiring to navigate to the location settings page
    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            Log.i(TAG, "All location settings are satisfied.");
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the result
                                // in onActivityResult().
                                status.startResolutionForResult(Design.this, GPS_REQUEST_CODE);
                            } catch (IntentSender.SendIntentException e) {
                                Log.i(TAG, "PendingIntent unable to execute request.");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                            break;
                    }
                }
            });
    }
    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        // locationDialog- if gps is turned on build the current location dialog
        if (resultCode == -1)
                buildDialogCurrentLocation();

        // contact- checks if a contact is selected or not
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be using multiple startActivityForResult
            switch (requestCode) {
                case CONTACT_REQUEST_CODE:
                    try {
                        String name;
                        Uri uri = data.getData();
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        int  nameIndex  = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                        name = cursor.getString(nameIndex);
                        Main.showToast("Sharing location with "+ name);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
            Main.showToast("Cancelled");

        }




        super.onActivityResult(requestCode, resultCode, data);



    }
    private BroadcastReceiver bReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            add = intent.getStringExtra(CONSTANT.ADDRESS);
        }
    };

    protected void onPause (){
        super.onPause();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }
}