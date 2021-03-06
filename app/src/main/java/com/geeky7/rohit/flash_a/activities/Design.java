/*
* This is the main activity of the app
 * Shows 6 layouts-sets onClicks
 * 2 action bar buttons- shares currentLocations- send sms, build dialog...
 * registers a broadcastReceiver which fetched address landmark and url from locationService2
* */
package com.geeky7.rohit.flash_a.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
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
import com.geeky7.rohit.flash_a.fragments.Keyword;
import com.geeky7.rohit.flash_a.services.LocationService2;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
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

    private static final String TAG = CONSTANT.DESIGN;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int GPS_REQUEST_CODE = 42; // the answer to universe
    private static final int CONTACT_REQUEST_CODE = 50;

    LinearLayout serviceEnabled_lay, homeAddress_lay, keyword_lay, customiseMessage_lay, history_lay, tutorial_lay;
    TextView serviceEnabled_tv;
    ImageView serviceEnabled_iv;

    SharedPreferences preferences;
    ProgressDialog progressDialog;

    Main m;

    String address ="NA";
    String placeS;
    String URL;

    static int counter_retry_fetching_location;
    private Keyword keyword = new Keyword();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        m = new Main(getApplicationContext());
        m.calledMethodLog(TAG,"onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.design);

        MobileAds.initialize(this, getResources().getString(R.string.ADMOB_APP_ID));

        AdView adView  = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("051300950451AB528C6889589C437FE5")
                .build();

        adView.loadAd(adRequest);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();

        //if app opened for the first time show tutorial activity
        boolean firstTime = preferences.getBoolean(CONSTANT.APP_OPENED_FIRST_TIME,true);
        if (firstTime){
            startActivity(new Intent(this,TutorialActivityFirstTime.class));
            editor.putBoolean(CONSTANT.APP_OPENED_FIRST_TIME,false).apply();
        }
        // checking if the permissions are not granted call request method which starts the procedure
        else if (!checkPermissions()) requestPermissions();

        progressDialog = new ProgressDialog(this);

        // finding view by id for all the associated views
        findViewById();

        // fetching the service status from the sharedPreference and checking if its enabled or not
        // calling respective methods
        final boolean service = preferences.getBoolean(CONSTANT.SERVICE, true);
        if (service) enableService();
        else disableService();

        //open keyword fragment onClick Keyword layout
        keyword_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!keyword.isAdded())
                    keyword.show(getFragmentManager(),"Keyword");
            }
        });
    }

    // find view by id of all the views
    private void findViewById() {
        m.calledMethodLog(TAG,"findViewById");
        serviceEnabled_tv = (TextView) findViewById(R.id.serviceEnabled_tv);
        serviceEnabled_iv = (ImageView) findViewById(R.id.serviceEnabled_iv);
        serviceEnabled_lay = (LinearLayout) findViewById(R.id.serviceEnabled_lay);
        homeAddress_lay = (LinearLayout) findViewById(R.id.homeAddress_lay);
        keyword_lay = (LinearLayout) findViewById(R.id.keyword_lay);
        customiseMessage_lay = (LinearLayout) findViewById(R.id.customiseMessage_lay);
        history_lay = (LinearLayout) findViewById(R.id.history_lay);
        tutorial_lay = (LinearLayout) findViewById(R.id.tutorial_lay);
    }

    // if the service is running and user hits the layout
    // this method runs and it changes the disables the service and changes the color of the layout to red
    public void disableService() {
        m.calledMethodLog(TAG,"disableService");
        serviceEnabled_lay.setBackgroundColor(Color.RED);
        serviceEnabled_tv.setText(R.string.service_disabled);
        serviceEnabled_iv.setImageBitmap(null);
        serviceEnabled_iv.setImageResource(R.drawable.service_disabled);
        serviceEnabled_iv.setScaleType(ImageView.ScaleType.FIT_XY);
        serviceEnabled_tv.setPadding(20, 20, 20, 20);
    }

    // if the service is not running this method is called
    // changes the color to orange or something for some reasons--> not now it's funny
    public void enableService() {
        m.calledMethodLog(TAG,"EnableService");

        serviceEnabled_lay.setBackgroundColor(Color.parseColor("#27e833"));
        serviceEnabled_tv.setText(R.string.service_enabled);
        serviceEnabled_iv.setImageBitmap(null);
        serviceEnabled_iv.setImageResource(R.drawable.service_enabled);
        serviceEnabled_iv.setScaleType(ImageView.ScaleType.FIT_XY);
        serviceEnabled_tv.setPadding(20, 20, 20, 20);
    }

    // this method checks if all the required permission are granted
    // returns a boolean
    private boolean checkPermissions() {
        m.calledMethodLog(TAG,"checkPermission");

        int locationPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int SMSPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS);
        int ContactsPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);

        return locationPermission == PackageManager.PERMISSION_GRANTED &&
                SMSPermission == PackageManager.PERMISSION_GRANTED &&
                ContactsPermission == PackageManager.PERMISSION_GRANTED;
    }

    // this method starts the location request
    // this method is called when the user has denied the permission once but has not checked Never ask againg checkbox
    private void requestPermissions() {
        m.calledMethodLog(TAG,"requestPermission");

        boolean shouldProvideRationaleLocation =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);
        boolean shouldProvideRationaleSMS =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_SMS);
        boolean shouldProvideRationaleContact =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS);
        m = new Main(getApplicationContext());

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.

        // if either location or SMS or contacts permission is not granted; request
        if (shouldProvideRationaleLocation || shouldProvideRationaleSMS || shouldProvideRationaleContact) {
            m.updateLog(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startPermissionRequest();
                        }
                    });
        } else {
            m.updateLog(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startPermissionRequest();
        }
    }

    // This method is called the first time the app is installed
    // requests all the permissions stated here
    private void startPermissionRequest() {
        m.calledMethodLog(TAG,"StartPermissionRequest");
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
        m.calledMethodLog(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                m.updateLog(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
            }
            if (grantResults[0] == PackageManager.PERMISSION_DENIED ||
                grantResults[1] == PackageManager.PERMISSION_DENIED ||
                grantResults[2] == PackageManager.PERMISSION_DENIED) {
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
                    }
                );
            }
        }
    }
    // Show why a not granted permission is required
    // no button snackbar
    private void showSnackbar(final String text) {
        m.calledMethodLog(TAG,"showSnackbar");
        Snackbar.make(findViewById(android.R.id.content),text,
                Snackbar.LENGTH_LONG)
                .show();
    }
    // shows a Snackbar indefinitely (for permissions)
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        m.calledMethodLog(TAG,"showSnackbar");
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private void showSnackbar2(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        m.calledMethodLog(TAG,"showSnackbar2");
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_LONG)
                .setAction(getString(actionStringId), listener).show();
    }

    // registers receiver to receive address, place name and url
    // sets onClick layout listeners
    @Override
    protected void onResume() {
        super.onResume();
        m.calledMethodLog(TAG,"onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("message"));
        startLocationService2();

        final SharedPreferences.Editor editor = preferences.edit();
        serviceEnabled_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean service = preferences.getBoolean(CONSTANT.SERVICE, true);
                if (service) {
                    Main.showToast("FL-ASHA disabled");
                    editor.putBoolean(CONSTANT.SERVICE, false);
                    editor.apply();
                    disableService();
                } else {
                    Main.showToast("FL-ASHA enabled");
                    editor.putBoolean(CONSTANT.SERVICE, true);
                    editor.apply();
                    enableService();
                }
            }
        });

//      open HomeAddress activity when home address layout is clicked
        homeAddress_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), HomeAddress.class));
            }
        });

        keyword_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!keyword.isAdded())
                    keyword.show(getFragmentManager(),"Keyword");
            }
        });

        // show keyword in a toast message when the keyword is clicked and hodl
        keyword_lay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Main.showToast("Your keyword is :"+ preferences.getString(CONSTANT.KEYWORD,"Asha"));
                return true;
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

    // method to start locationService2
    private void startLocationService2() {
        m.calledMethodLog(TAG,"startLocationService2");

        stopService(new Intent(this, LocationService2.class));
        startService(new Intent(this, LocationService2.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        m.calledMethodLog(TAG,"onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // shows settings and currentLocation buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        m.calledMethodLog(TAG,"onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_current_location:
                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean b = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean internet = m.isNetworkAvailable();

                if(!checkPermissions()) requestPermissions();

                //if no internet show a snackbar informing a user
                if (!internet){
                    showSnackbar("No internet Connectivity. Please connect to a network and retry");
                }
                // if the gps is off
                else if (!b) {
                    // calls this method which open the android location dialog which ask to enable location
                    // and enables the location when the user clicks ok
                    new buildLocationDialog(this).execute();
                } else {
                    new YourAsyncTask(this).execute();// gps is on
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void buildDialogCurrentLocation() {
        m.calledMethodLog(TAG,"buildDialogCurrentLocation");
        final AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Your Current Location")
            .setMessage(address +" (Near " + placeS +")")
            .setIcon(android.R.drawable.ic_menu_mylocation)
            //setting up buttons
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
                    Main.showToast("Select contact to share your current location");
        }
            })
        .setNeutralButton("Map", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        final AlertDialog dialog = builder.create();
        // if address is fetched show dialog
        if (!("NA").equals(address)){
            dialog.show();
//            builder.show();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lat = preferences.getString(CONSTANT.LATITUDE,"");
                String lng = preferences.getString(CONSTANT.LONGITUDE,"");

                Intent viewOnMap = new Intent(Design.this,CurrentLocationMapActivity.class);
                viewOnMap.putExtra(CONSTANT.ADDRESS,address);
                viewOnMap.putExtra(CONSTANT.LATITUDE,lat);
                viewOnMap.putExtra(CONSTANT.LONGITUDE,lng);
                startActivity(viewOnMap);
            }
        });
            counter_retry_fetching_location = 0;
            m.updateLog(TAG,"address is "+address);
        }
        // else retry like 6 times before showing an error snackbar and reset the counter
        else{
            m.updateLog(TAG,"address iss " + address);
            m.calledMethodLog(TAG,"Retry number: "+counter_retry_fetching_location);
            if (counter_retry_fetching_location++==2){
                counter_retry_fetching_location=0;
                showSnackbar2(R.string.error_fetching_location, R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new YourAsyncTask(Design.this).execute();// snackbar- error
                    }
                });
            }
            else
                new YourAsyncTask(Design.this).execute(); // if no address and counter < 5
        }
        stopService(new Intent(this, LocationService2.class));
    }

    // onClick currentLocation button in actionBar and gps is off
    // opens a dialog which turns on gps without requiring to navigate to the location settings page
    private void displayLocationSettingsRequest(Context context) {
        m.calledMethodLog(TAG,"displayLocationSettingsRequest");

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000/2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        m.updateLog(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        m.updateLog(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(Design.this, GPS_REQUEST_CODE);
                            m.updateLog(TAG,"starting resolution one.");
                            progressDialog.setMessage("Turning on GPS");
                            //progressDialog.show();

                            /*Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    progressDialog.dismiss();
                                }
                            }, 5000); // 5 seconds delay*/

                        } catch (IntentSender.SendIntentException e) {
                            m.updateLog(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        m.updateLog(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }
    // checks for the name of the contact, calls sendSMS and also buildCurrentLocationDialog
    // yeah a lot of works looks like never had problems with this part of the code
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        m.calledMethodLog(TAG,"onActivityResult");
        //progressDialog.dismiss();
        // check if the gps is enabled and a contact is selected
        // if either of them is true this if runs
        if (resultCode == RESULT_OK) {
            // Check for the request code, multiple startActivityForResult
            switch (requestCode) {
                // contact- checks if a contact is selected or not
                case CONTACT_REQUEST_CODE:
                    Cursor cursor;
                    try {
                        Uri uri = data.getData();
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        int phoneIndex  = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        int  nameIndex  = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                        String phone = cursor.getString(phoneIndex);
                        String name = cursor.getString(nameIndex);
                        sendSMS(phone,name);
                        Main.showToast("Sharing location with "+ name);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                // locationDialog- if gps is turned on build the current location dialog
                case GPS_REQUEST_CODE:
                    m.updateLog(TAG,"I'm on onActivity result for gps request code. Say Hi to me :)");
                    new YourAsyncTask(this).execute(); // when gps was off and now is on after dialog
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // sends an SMS. Enough said
    // no eta just address and landmark
    private void sendSMS(String sender,String name) {
        m.calledMethodLog(TAG,"sendSMS");
        SmsManager manager = SmsManager.getDefault();
        boolean landmark = preferences.getBoolean(getResources().getString(R.string.settings_landmark),false);
        if (!landmark) placeS = "";
        else{
            placeS = "Near "+ placeS;
            address = " ("+ address + ").";
        }
        String message = placeS+ address+ " " + URL;
        manager.sendTextMessage(sender,null, message, null, null);

        boolean notification = preferences.getBoolean(getResources()
                .getString(R.string.settings_notification),false);
        if (notification)
            m.pugNotification("Location shared","Your current location shared with",name);
    }

    // when the sendBroadcast method is called in locationService this code runs
    // gets the address and the place name
    private BroadcastReceiver bReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            m.calledMethodLog(TAG,"bReceiver");
            address = intent.getStringExtra(CONSTANT.ADDRESS);
            placeS = intent.getStringExtra(CONSTANT.PLACE_NAME);
            URL = intent.getStringExtra(CONSTANT.URL_SHORTNER_SHARE_LOCATION);
        }
    };
    // unregister the Broadcast receiver
    protected void onPause (){
        m.calledMethodLog(TAG,"onPause");
        super.onPause();
        stopService(new Intent(this, LocationService2.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }

    private class YourAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        public YourAsyncTask(Design activity) {
            dialog = new ProgressDialog(activity);
        }
        @Override
        protected void onPreExecute() {
            String message = getResources().getString(R.string.dialog_message);
            if (counter_retry_fetching_location==1) message = getResources().getString(R.string.dialog_message_retry_1);
            if (counter_retry_fetching_location>1) message = getResources().getString(R.string.dialog_message_retry_2_plus);

            dialog.setMessage(message);
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
        }
        protected Void doInBackground(Void... args) {
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startLocationService2();
            //Note- sleep here removed was causing - skipping frames
            return null;
        }
        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) dialog.dismiss();
            buildDialogCurrentLocation();
        }
    }

    private class buildLocationDialog extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        public buildLocationDialog(Design activity) {
            dialog = new ProgressDialog(activity);
        }
        @Override
        protected void onPreExecute() {
            String message = "Turning on GPS";

            dialog.setMessage(message);
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
        }
        protected Void doInBackground(Void... args) {
            displayLocationSettingsRequest(Design.this);
            return null;
        }
        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) dialog.dismiss();
        }
    }
}