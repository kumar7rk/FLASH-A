// new written
package com.geeky7.rohit.flash_a.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.R;
import com.geeky7.rohit.flash_a.fragments.ContactsFragment;

public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // adds a fragment which extract a preference screen
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Main.showToast("I'm called " + s);
        boolean eta = sharedPreferences.getBoolean(getResources().getString(R.string.settings_send_eta),false);
        if (eta) Main.showToast("eta on");
        else Main.showToast("eta off");
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // getting the preference data from xml
            addPreferencesFromResource(R.xml.settings);
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            final Preference eta = findPreference("Settings_Send_ETA");
            final Preference contacts = findPreference("Settings_Select_Contacts");

            boolean b = preferences.getBoolean("Settings_Send_ETA",false);
            if (!b) contacts.setEnabled(false);
            if (b) contacts.setEnabled(true);

            eta.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                boolean b = preferences.getBoolean("Settings_Send_ETA",false);
                    if (b) contacts.setEnabled(false);
                    if (!b) contacts.setEnabled(true);
                    return true;
                }
            });
            contacts.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ContactsFragment contact = new ContactsFragment();
                    if (!contact.isAdded())
                        contact.show(getFragmentManager(),"Contacts");
                    return false;
                }
            });

        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            // going back to design activity when the back arrow is clicked
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
