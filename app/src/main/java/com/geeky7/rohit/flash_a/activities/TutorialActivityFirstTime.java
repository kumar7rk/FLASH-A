/*
Shows a tutorial
on back press closed the activity
*/

package com.geeky7.rohit.flash_a.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.geeky7.rohit.flash_a.R;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.MessageButtonBehaviour;
import agency.tango.materialintroscreen.SlideFragmentBuilder;
import agency.tango.materialintroscreen.animations.IViewTranslation;

public class TutorialActivityFirstTime extends MaterialIntroActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableLastSlideAlphaExitTransition(true);

        getBackButtonTranslationWrapper()
                .setEnterTranslation(new IViewTranslation() {
                    @Override
                    public void translate(View view, @FloatRange(from = 0, to = 1.0) float percentage) {
                        view.setAlpha(percentage);
                    }
                });

        /*Whether you're driving or away from your phone. you'll always be closer to them with ASHA app.
        " +
        "ASHA sends your location, including address and landmark, on receiving your customised keyword in an SMS.*/
                //  Html.fromHtml("<b>" + myText + "</b>"

        // first fragment
        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.first_slide_background)
                        .buttonsColor(R.color.first_slide_buttons)
                        .image(R.drawable.tutorial_slide_1)
                        .title("Stay close to your dear ones!")
                        .description("Whether you're driving or could not reach your your phone, you'll always be closer to them with ASHA app.")
                        .build()
        );
        // second fragment
        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.second_slide_background)
                .buttonsColor(R.color.second_slide_buttons)
                .image(R.drawable.tutorial_slide_2)
                .title("Receive an SMS request. Share location.")
//                .title("Receive location request in SMS. Automatically share your location.")
                .description("\n Whenever you receive an SMS with your chosen keyword. Your current location will be shared with them. " +
                        "\n\n"+
                        "You'll be notified when your location is shared.")
                .build()
        );
        // third fragment
        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.third_slide_background)
                        .buttonsColor(R.color.third_slide_buttons)
                        .image(R.drawable.tutorial_slide_3)
                        .title("Stay safe & in control")
                        .description("You can set your home address to send ETA home , edit keyword for your convenience." +
                                "\n\n"+
                                "You're in complete control when the app runs. You can enable or disable the app with one click when you want privacy.")
                        .build()
        );
        // fourth fragment
        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.fourth_slide_background)
                .buttonsColor(R.color.fourth_slide_buttons)
                .image(R.drawable.tutorial_slide_4)
                .title("Running into an emergency?")
                .description("No worries! You can share your current location including address and nearby landmark via SMS in under 30 seconds.")
                .build()
                ,
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!checkPermissions()) requestPermissions();
                    }
                }, "Grant Permissions!")
                );
    }
    // back button press closes the tutorial activity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    // this method checks if all the required permission are granted
    // returns a boolean
    private boolean checkPermissions() {
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

        // if either location or SMS or contacts permission is not granted; request
        if (shouldProvideRationaleLocation || shouldProvideRationaleSMS || shouldProvideRationaleContact) {
            //m.updateLog(TAG, "Displaying permission rationale to provide additional context.");
            /*showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startPermissionRequest();
                        }
                    });*/
        } else {
            //m.updateLog(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startPermissionRequest();
        }
    }

    // This method is called the first time the app is installed
    // requests all the permissions stated here
    private void startPermissionRequest() {
        ActivityCompat.requestPermissions(TutorialActivityFirstTime.this,
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
        //m.updateLog(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                //m.updateLog(TAG, "User interaction was cancelled.");
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
                /*showSnackbar(R.string.permission_denied_explanation, R.string.settings,
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
                );*/
            }
        }
    }
    /*// shows a Snackbar indefinitely (for permissions)
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_SHORT)
                .setAction(getString(actionStringId), listener).show();
    }*/
}
