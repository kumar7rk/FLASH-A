/*
Shows a tutorial

*/

package com.geeky7.rohit.flash_a.activities;

import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.view.View;

import com.geeky7.rohit.flash_a.R;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragmentBuilder;
import agency.tango.materialintroscreen.animations.IViewTranslation;

public class TutorialActivity extends MaterialIntroActivity {

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
        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.first_slide_background)
                        .buttonsColor(R.color.first_slide_buttons)
                        .image(R.drawable.tutorial1)
                        .title("Stay close to your close ones!")
                        .description("Whether you're driving or away from your phone you'll always be close to them with ASHA app.")
                        .build()
              /*  ,
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMessage("We provide solutions to make you love your work");
                    }
                }, "Work with love")*/
                );

        //It works! It's that simple!
        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.second_slide_background)
                .buttonsColor(R.color.second_slide_buttons)
                .title("Receive an SMS. Share location.")
                .description("\n Whenever you receive an SMS with your chosen keyword. Your location will be shared with the sender of the message. " +
                        "\n"+
                        "You'll be notified when your location is shared")
                .build());

//        addSlide(new CustomSlide());

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.third_slide_background)
                        .buttonsColor(R.color.third_slide_buttons)
//                        .image(R.drawable.img_equipment)
                        .title("Stay safe & in control")
                        .description("You can set your home address to send home ETA, edit keyword for your convenience." +
                                "\n"+
                                "You're in complete control when the app runs. You can turn in off with just one click when you want privacy.")
                        .build()
                /*,
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMessage("Try us!");
                    }
                }, "Tools")*/
                );

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.fourth_slide_background)
                .buttonsColor(R.color.fourth_slide_buttons)
                .title("Running into an emergency?")
                .description("You can share your current location with anyone with just a few clicks.")
                .build());
    }
}
