// new written
package com.geeky7.rohit.flash_a.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.Switch;

import com.geeky7.rohit.flash_a.R;

public class SettingsActivity extends AppCompatPreferenceActivity{

    SharedPreferences preferences;
    static Switch sendEtaS;
    Switch landmarkS;
    CheckBox notificationCB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

//        sendEtaS = (Switch) findViewById(R.id.settings_send_eta);
//        notificationCB = (CheckBox) findViewById(R.id.settings_notification);
//        landmarkS = (Switch) findViewById(R.id.settings_landmark);


//        boolean eta = preferences.getBoolean(getResources().getString(R.string.settings_send_eta),false);
//        boolean notification =  preferences.getBoolean(getResources().getString(R.string.settings_send_eta),false);
//        boolean landmark = preferences.getBoolean(getResources().getString(R.string.settings_send_eta),false);


//        sendEtaS.setSelected(eta);
//        notificationCB.setChecked(notification);
//        landmarkS.setSelected(landmark);
//        SharedPreferences prefs = this.getSharedPreferences("settings", 0);

        final SharedPreferences.Editor editor = preferences.edit();

        /*SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                Main.showToast("Called. And and done for day");
                boolean value = preferences.getBoolean(key,false);
                editor.putBoolean(key,value).apply();

                *//*if (key.equals(getResources().getString(R.string.settings_send_eta))){
                }
                if (key.equals(getResources().getString(R.string.settings_notification))){

                }
                if (key.equals(getResources().getString(R.string.settings_landmark))){

                }*//*

            }
        };

        preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);*/

    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

//            View v =

//            sendEtaS = (Switch) getView().findViewById(R.id.settings_send_eta);

        }
    }


}
