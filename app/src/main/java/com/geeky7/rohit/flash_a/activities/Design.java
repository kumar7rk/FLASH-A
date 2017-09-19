package com.geeky7.rohit.flash_a.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.R;

import static com.geeky7.rohit.flash_a.R.id.serviceEnabled;
import static com.geeky7.rohit.flash_a.R.id.serviceEnabled_lay;
import static com.geeky7.rohit.flash_a.R.id.serviceEnabled_tv;

public class Design extends AppCompatActivity {

    LinearLayout serviceEnabled_lay;
    TextView serviceEnabled_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.design);
        serviceEnabled_lay = (LinearLayout)findViewById(serviceEnabled_lay);
        serviceEnabled_tv = (TextView)findViewById(R.id.serviceEnabled_tv);

        serviceEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceEnabled();
            }
        });
    }
    public void serviceEnabled(){
        Main.showToast("Service Enabled click");
        serviceEnabled_lay.setBackgroundColor(Color.RED);
        serviceEnabled_tv.setText("Service Disabled");
    }
}
