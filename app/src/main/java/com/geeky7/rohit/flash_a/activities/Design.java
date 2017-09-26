package com.geeky7.rohit.flash_a.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geeky7.rohit.flash_a.BuildConfig;
import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.R;

public class Design extends AppCompatActivity {

    private static final String TAG = Design.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    boolean locationPermission = true;

    LinearLayout serviceEnabled_lay,homeAddress_lay,keyword_lay,customiseMessage_lay,history_lay, tutorial_lay;
    TextView serviceEnabled_tv;
    ImageView serviceEnabled_iv;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.design);

        if(!checkPermissions())
            requestPermissions();

        serviceEnabled_tv = (TextView)findViewById(R.id.serviceEnabled_tv);
        serviceEnabled_iv = (ImageView)findViewById(R.id.serviceEnabled_iv);

        serviceEnabled_lay = (LinearLayout)findViewById(R.id.serviceEnabled_lay);
        homeAddress_lay = (LinearLayout)findViewById(R.id.homeAddress_lay);
        keyword_lay = (LinearLayout)findViewById(R.id.keyword_lay);
        customiseMessage_lay = (LinearLayout)findViewById(R.id.customiseMessage_lay);
        history_lay = (LinearLayout)findViewById(R.id.history_lay);
        tutorial_lay = (LinearLayout)findViewById(R.id.tutorial_lay);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean service = preferences.getBoolean("service",true);

        if(service) enableService();
        else disableService();

        homeAddress_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),HomeAddress.class));
            }
        });
    }

    public void disableService(){
        serviceEnabled_lay.setBackgroundColor(Color.RED);
        serviceEnabled_tv.setText(R.string.service_disabled);
        serviceEnabled_iv.setImageBitmap(null);
        serviceEnabled_iv.setImageResource(R.drawable.service_disabled);
        serviceEnabled_iv.setScaleType(ImageView.ScaleType.FIT_XY);
        serviceEnabled_tv.setPadding(20,20,20,20);
    }

    public void enableService(){
        serviceEnabled_lay.setBackgroundColor(0xffff8800);
        serviceEnabled_tv.setText(R.string.service_enabled);
        serviceEnabled_iv.setImageBitmap(null);
        serviceEnabled_iv.setImageResource(R.drawable.service_enabled);
        serviceEnabled_iv.setScaleType(ImageView.ScaleType.FIT_XY);
        serviceEnabled_tv.setPadding(20,20,20,20);
    }
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

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
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
    private void showSnackbar(final String text) {
        View container = findViewById(R.id.main_activity_container);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }
    private void startPermissionRequest() {
        ActivityCompat.requestPermissions(Design.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_CONTACTS},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }
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
            } else {
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
        editor.putBoolean("locationPermission",locationPermission);
        editor.commit();
    }
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences.Editor editor = preferences.edit();
        serviceEnabled_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean service = preferences.getBoolean("service",true);
                if(service){
                    Main.showToast("FL-ASHA disabled");
                    editor.putBoolean("service",false);
                    editor.apply();
                    disableService();
                }
                else {
                    Main.showToast("FL-ASHA enabled");
                    editor.putBoolean("service",true);
                    editor.apply();
                    enableService();
                }
            }
        });

        keyword_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Main.showToast("Coming Soon!");
            }
        });

        customiseMessage_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Main.showToast("Coming Soon!");
            }
        });

        history_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Main.showToast("Coming Soon!");
            }
        });

        tutorial_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Main.showToast("Coming Soon!");
            }
        });
    }
}
