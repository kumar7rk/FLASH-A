package com.geeky7.rohit.flash_a.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.R;

public class Design extends AppCompatActivity {

    LinearLayout serviceEnabled_lay;
    TextView serviceEnabled_tv;
    ImageView serviceEnabled_iv;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.design);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        final boolean service = preferences.getBoolean("service",true);

        serviceEnabled_lay = (LinearLayout)findViewById(R.id.serviceEnabled_lay);
        serviceEnabled_tv = (TextView)findViewById(R.id.serviceEnabled_tv);
        serviceEnabled_iv = (ImageView)findViewById(R.id.serviceEnabled_iv);
        if(service) enableService();
        else disableService();
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
                    editor.commit();
                    disableService();
                }
                else {
                    Main.showToast("FL-ASHA enabled");
                    editor.putBoolean("service",true);
                    editor.commit();
                    enableService();
                }
            }
        });
    }
}
