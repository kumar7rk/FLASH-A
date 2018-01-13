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
import android.provider.ContactsContract;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.R;

import java.util.ArrayList;

public class ContactsFragment extends DialogFragment {
    SharedPreferences preferences;
    AlertDialog.Builder alertDialog;
    private ListView listView;
    private ArrayList<Contact> contacts = null;
    private ProgressDialog progressDialog = null;
    private ContactAdapter contactAdapter = null;
    private Runnable viewContacts = null;
    private SparseBooleanArray selectedContacts =   new SparseBooleanArray()  ;

    public ContactsFragment() {
    }

    //sets up the dialog; pretty much does everything this class is expected too

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.list_view_contact, null);
        listView = (ListView)view.findViewById(R.id.list_view);

        contacts = new ArrayList<>();
        getContacts();
        contactAdapter = new ContactAdapter(getActivity(), R.layout.main2, contacts);
        listView.setAdapter(contactAdapter);

        alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Select Contacts")
                .setView(view)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return alertDialog.create();
    }
    //interacts with android and steals all the contacts; I mean fetch
    private void getContacts(){
        try{
            String[] projection = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER,
                    ContactsContract.Contacts._ID
            };
            Cursor cursor = getActivity().managedQuery(ContactsContract.Contacts.CONTENT_URI, projection, ContactsContract.Contacts.HAS_PHONE_NUMBER+"=?", new String[]{"1"}, ContactsContract.Contacts.DISPLAY_NAME);
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
        public ContactAdapter(Context context, int textViewResourceId, ArrayList<Contact> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.main2, null);
            }
            Contact contact = items.get(position);
            if (contact != null) {
                CheckBox nameCheckBox = (CheckBox) view.findViewById(R.id.cb_app);
                nameCheckBox.setChecked(selectedContacts.get(position));
                if (nameCheckBox != null) {
                    nameCheckBox.setText(contact.getContactName());
                }
                nameCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        Main.showToast(compoundButton.getText().toString()+ " selected");
                    }
                });
            }
            return view;
        }
    }
    // contact class just for fun. No it's the backbone of the whole idea. But I don't know how it works. But It does.
    public class Contact {
        private String contactName;

        public String getContactName() {
            return contactName;
        }
        public void setContactName(String contactName) {
            this.contactName = contactName;
        }
    }
}