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

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.first_slide_background)
                        .buttonsColor(R.color.first_slide_buttons)
                        .image(R.drawable.tutorial1)
                        .title("End the Worries of your closed ones?")
                //  Html.fromHtml("<b>" + myText + "</b>"
                        .description("Whether you're driving or away from your phone. you'll always be closer to them with ASHA app.\n" +
                                "ASHA sends your location, including address and landmark, on receiving your customised keyword.")
                        .build()
              /*  ,
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMessage("We provide solutions to make you love your work");
                    }
                }, "Work with love")*/
                );

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.second_slide_background)
                .buttonsColor(R.color.second_slide_buttons)
                .title("Share your current location automatically with your family member using your chosen keyword for incoming SMS")
                .description("It works! It's that simple!")
                .build());

//        addSlide(new CustomSlide());

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.third_slide_background)
                        .buttonsColor(R.color.third_slide_buttons)
//                        .image(R.drawable.img_equipment)
                        .title("Want more?")
                        .description("Set home address to send home ETA. Customise message, check history and more.")
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
                .title("In an emergency?")
                .description("Share your current location with the click of a button")
                .build());
    }
}
