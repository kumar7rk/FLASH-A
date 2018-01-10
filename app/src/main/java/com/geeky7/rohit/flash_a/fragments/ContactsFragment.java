package com.geeky7.rohit.flash_a.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.main2, null);
        listView = (ListView)view.findViewById(android.R.id.list);

        contacts = new ArrayList<>();
        getContacts();
        contactAdapter = new ContactAdapter(getActivity(), R.layout.activity_contacts, contacts);
        listView.setAdapter(contactAdapter);

        //on click save button save the keyword in the sharedPreferences
        alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Select Contacts")
                .setView(inflater.inflate(R.layout.main2, null))
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                // close button to close the dialog
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        contactAdapter.notifyDataSetChanged();
//        new LoadContacts().execute();


        return alertDialog.create();
    }

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
                view = vi.inflate(R.layout.activity_contacts, null);
            }
            Contact contact = items.get(position);
            if (contact != null) {
                CheckBox nameCheckBox = (CheckBox) view.findViewById(R.id.checkBox);
                nameCheckBox.setChecked(selectedContacts.get(position));
                if (nameCheckBox != null) {
                    nameCheckBox.setText(contact.getContactName());
                }
                nameCheckBox.setOnClickListener((View.OnClickListener) new OnItemClickListener(position,nameCheckBox.getText(),nameCheckBox));
            }
            return view;
        }
    }

    class OnItemClickListener implements DialogInterface.OnClickListener {
        private int position;
        private CharSequence text;
        private CheckBox checkBox;
        OnItemClickListener(int position, CharSequence text,CheckBox checkBox){
            this.position = position;
            this.text = text;
            this.checkBox = checkBox;
        }
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            selectedContacts.append(position, true);
//            Toast.makeText(getBaseContext(), "Clicked "+position +" and text "+text,Toast.LENGTH_SHORT).show();
        }
    }

    public class Contact {
        private String contactName;

        public String getContactName() {
            return contactName;
        }
        public void setContactName(String contactName) {
            this.contactName = contactName;
        }
    }
    class LoadContacts extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Do Dialog stuff here
            progressDialog = ProgressDialog.show(getActivity(),"Please wait...", "Retrieving contacts ...", true);
        }
        protected String doInBackground(String... args) {
            // Put your implementation to retrieve contacts here
            getContacts();
            return null;
        }
        protected void onPostExecute(String file_url) {
            contactAdapter = new ContactAdapter(getActivity(), R.layout.activity_contacts, contacts);
            listView.setAdapter(contactAdapter);

            // Dismiss Dialog
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            contactAdapter.notifyDataSetChanged();
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    // Create list adapter and notify it
                }
            });

        }

    }
}