/*
Shows a tutorial
on back press closed the activity
*/

package com.geeky7.rohit.flash_a.activities;

import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.view.View;

import com.geeky7.rohit.flash_a.CONSTANT;
import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.R;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragmentBuilder;
import agency.tango.materialintroscreen.animations.IViewTranslation;

public class TutorialActivity extends MaterialIntroActivity {
    private static final String TAG = CONSTANT.TUTORIAL_ACTIVITY;
    Main m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        m = new Main(getApplicationContext());
        m.calledMethodLog(TAG, "onCreate");

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
                .description("\n Whenever you receive an SMS with your chosen keyword. Your current location will be shared via SMS. " +
                        "\n\n"+
                        "You can also chose to be notified by a push notification.")
                .build()
        );
        // third fragment
        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.third_slide_background)
                        .buttonsColor(R.color.third_slide_buttons)
                        .image(R.drawable.tutorial_slide_3)
                        .title("Stay safe & in control")
                        .description("You can set your home (or any) address to share travel times with chosen contacts." +
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
                /*,
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }, "Stay Connected!")*/
                );
    }
    // back button press closes the tutorial activity
    @Override
    public void onBackPressed() {
        m.calledMethodLog(TAG, "onBackPressed");
        super.onBackPressed();
        finish();
    }
}
