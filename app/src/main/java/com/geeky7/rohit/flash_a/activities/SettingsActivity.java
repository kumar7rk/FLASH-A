// new written
package com.geeky7.rohit.flash_a.activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.geeky7.rohit.flash_a.R;
import com.geeky7.rohit.flash_a.fragments.ContactsFragment;

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

            Preference eta = findPreference("Settings_Send_ETA");
            eta.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
                @Override
                public boolean onPreferenceClick(Preference preference){
                    ContactsFragment contact = new ContactsFragment();
                    if (!contact.isAdded())
                        contact.show(getFragmentManager(),"Contacts");
                    return true;
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
