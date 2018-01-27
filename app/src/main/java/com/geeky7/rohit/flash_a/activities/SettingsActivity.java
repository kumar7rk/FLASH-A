// new written
// shows lists and pretty much anything else for a settingsActivity
package com.geeky7.rohit.flash_a.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.geeky7.rohit.flash_a.CONSTANT;
import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.R;
import com.geeky7.rohit.flash_a.fragments.ContactsFragment;

import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // adds a fragment which extract a preference screen
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

        // adding support for actionbar back button at the top
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // apparently this method is not being called
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Main.showToast("I'm called " + s);
        boolean eta = sharedPreferences.getBoolean(getResources().getString(R.string.settings_send_eta),false);
        if (eta) Main.showToast("eta on");
        else Main.showToast("eta off");
    }

    // a new fragment inner class which contains the layout
    public static class SettingsFragment extends PreferenceFragment {
        // toggles and changes summary Select Contacts preference
        //
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            // getting the preference data from xml
            addPreferencesFromResource(R.xml.settings);

            final Preference eta = findPreference("Settings_Send_ETA");
            final Preference contacts = findPreference("Settings_Select_Contacts");

            boolean b = preferences.getBoolean("Settings_Send_ETA",false);
            if (!b) contacts.setEnabled(false);
            if (b) contacts.setEnabled(true);

            setSelectContactsSummary(preferences, contacts);

            // eta toggles- enable/disable select contacts
            eta.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                boolean b = preferences.getBoolean("Settings_Send_ETA",false);
                    if (b) contacts.setEnabled(false);
                    if (!b) contacts.setEnabled(true);
                    return true;
                }
            });
            // onLick Select Contacts shows contactsFragment
            // returning true means that we have handle the onClick event
            contacts.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ContactsFragment contact = new ContactsFragment();
                    if (!contact.isAdded())
                        contact.show(getFragmentManager(),"Contacts");
                    setSelectContactsSummary(preferences,contacts);
                    return true;
                }
            });
        }

        // change the summary for select contacts when the chosen contacts are updated
        private void setSelectContactsSummary(SharedPreferences preferences, Preference contacts) {
            Set<String> set = new HashSet<>();
            set = preferences.getStringSet(CONSTANT.SELECTED_CONTACTS,set);
            String s1 = "";

            // combine the selected contacts in a string
            for (String s : set)
                s1 +=s+", ";

            // if no contact is selected show "None"
            if (set.size()==0) s1 = "None";
            // remove the last comma when adding contact in the string
            else if (s1.endsWith(", "))
                s1 = s1.substring(0,s1.length()-2);
            contacts.setSummary(s1);
        }
    }
    // Beam me back, Scotty to Design activity
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