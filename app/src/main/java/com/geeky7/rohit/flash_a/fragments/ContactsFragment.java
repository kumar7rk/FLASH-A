/*
Shows all the contacts in a dialog which doesn't beautiful at all
inspired from manualDialogFragment class in A-SUM
*/
package com.geeky7.rohit.flash_a.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import com.geeky7.rohit.flash_a.CONSTANT;
import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.MyApplication;
import com.geeky7.rohit.flash_a.R;
import com.geeky7.rohit.flash_a.activities.SettingsActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ContactsFragment extends DialogFragment {
    public static final String TAG = CONSTANT.CONTACTS_FRAGMENT;
    SharedPreferences preferences;
    AlertDialog.Builder alertDialog;

    private ListView listView;
    private ArrayList<Contact> contacts = null;
    private ContactAdapter contactAdapter = null;
    private SparseBooleanArray selectedContacts =   new SparseBooleanArray()  ;

    private ProgressDialog progressDialog = null;

    Set<String> selectedContactsS = new HashSet<>();
    Set<String> selectedContactsIndex = new HashSet<>();

    Main m;

    public ContactsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        m = new Main(MyApplication.getAppContext());
        m.calledMethodLog(TAG,"onCreate");

        super.onCreate(savedInstanceState);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        m.calledMethodLog(TAG,"onViewCreated");

        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        m.calledMethodLog(TAG,"onActivityCreated");

        super.onActivityCreated(savedInstanceState);
    }

    /* doing most of the work
    *
    * */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        m.calledMethodLog(TAG,"onCreateDialog");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.list_view_contact, null);

        listView = (ListView)view.findViewById(R.id.list_view);

        // setup shared preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = preferences.edit();

        //fetching saved contacts from shared preferences
        selectedContactsS = preferences.getStringSet(CONSTANT.SELECTED_CONTACTS,selectedContactsS);
        selectedContactsIndex = preferences.getStringSet(CONSTANT.SELECTED_CONTACTS_INDEX,selectedContactsIndex);

        //setting up adapter
        contacts = new ArrayList<>();
        getContacts();
        contactAdapter = new ContactAdapter(getActivity(), R.layout.main2, contacts);
        listView.setAdapter(contactAdapter);

        //putting selected contact's index in SparseBooleanArray
        for (String s: selectedContactsIndex)
            selectedContacts.append(Integer.parseInt(s),true);

        //setting up the dialog to show with buttons
        alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Select Contacts")
                .setView(view)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        editor.putStringSet(CONSTANT.SELECTED_CONTACTS,selectedContactsS);
                        editor.putStringSet(CONSTANT.SELECTED_CONTACTS_INDEX,selectedContactsIndex);
                        editor.apply();
                        getFragmentManager().beginTransaction().replace(android.R.id.content,new SettingsActivity.SettingsFragment())
                                .commit();
                    }
                })
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return alertDialog.create();
    }

    public static ContactsFragment newInstance(int title) {
        //m.calledMethodLog(TAG,"newInstance");

        ContactsFragment dialog = new ContactsFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        dialog.setArguments(args);
        return dialog;
    }
    //interacts with android and steals all the contacts; I mean fetch
    private void getContacts(){
        m.calledMethodLog(TAG,"getContacts");

        try{
            String[] projection = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER,
                    ContactsContract.Contacts._ID
            };
            Cursor cursor = getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, ContactsContract.Contacts.HAS_PHONE_NUMBER+"=?", new String[]{"1"}, ContactsContract.Contacts.DISPLAY_NAME);
            contacts = new ArrayList<Contact>();
            while(cursor.moveToNext()){
                Contact contact = new Contact();
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                contact.setContactName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                contacts.add(contact);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    // Adapter!! Yeah!! Everything in the same class
    // As long as it does it jobs you don't mind. Right?
    public class ContactAdapter extends ArrayAdapter<Contact> {
        private ArrayList<Contact> items;
        public static final String TAG = CONSTANT.CONTACT_ADAPTER;

        public ContactAdapter(Context context, int textViewResourceId, ArrayList<Contact> items) {
            super(context, textViewResourceId, items);
            m.calledMethodLog(TAG,"ContactAdapter");
            this.items = items;
        }
        // adding the layouts files and setting the contacts name to the dialog
        // setting up onClick listener as well
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            m.calledMethodLog(TAG,"getView");
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.main2, null);
            }
            Contact contact = items.get(position);
            // set onClickListener, select any selected contacts and also set the text of the contacts
            if (contact != null) {
                CheckBox nameCheckBox = (CheckBox) view.findViewById(R.id.cb_app);
                nameCheckBox.setText(contact.getContactName());
                nameCheckBox.setChecked(selectedContacts.get(position));
                nameCheckBox.setOnClickListener(new OnItemClickListener(position,nameCheckBox.getText(),nameCheckBox));
            }
            return view;
        }
    }
    // handles the onClickListener added above
    class OnItemClickListener implements View.OnClickListener {
        private int position;
        private CharSequence text;
        private CheckBox checkBox;
        OnItemClickListener(int position, CharSequence text,CheckBox checkBox){

            m.calledMethodLog(TAG,"onItemClickListener");
            this.position = position;
            this.text = text;
            this.checkBox = checkBox;
        }
        @Override
        public void onClick(View arg0) {
            m.calledMethodLog(TAG,"onClick");

            String contact = checkBox.getText().toString();
            boolean b = checkBox.isChecked();
            if (b){
                selectedContacts.append(position, true);
                selectedContactsIndex.add(position+"");
                selectedContactsS.add(contact);
            }
            else{
                selectedContacts.append(position, false);
                //Main.showToast(contact+ " removed");
                selectedContactsIndex.remove(position+"");
                selectedContactsS.remove(contact);
            }
        }
    }
    // contact class just for fun. No it's the backbone of the whole idea. But I don't know how it works. But It does.
    public class Contact {
        public static final String TAG = CONSTANT.CONTACT;

        private String contactName;

        public String getContactName(){
            m.calledMethodLog(TAG,"getContactName");
            return contactName;
        }
        public void setContactName(String contactName) {
            m.calledMethodLog(TAG,"setContactName");
            this.contactName = contactName;
        }
    }
}