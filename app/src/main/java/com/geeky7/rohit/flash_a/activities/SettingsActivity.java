// new written
package com.geeky7.rohit.flash_a.activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.geeky7.rohit.flash_a.R;

public class SettingsActivity extends AppCompatPreferenceActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // adds a fragment which extract a preference screen
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // getting the preference data from xml
            addPreferencesFromResource(R.xml.settings);
        }
    }
}
