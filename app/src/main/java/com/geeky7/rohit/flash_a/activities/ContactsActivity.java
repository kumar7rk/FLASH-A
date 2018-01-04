package com.geeky7.rohit.flash_a.activities;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

import com.geeky7.rohit.flash_a.R;

import java.util.ArrayList;

public class ContactsActivity extends ListActivity {
    private ArrayList<Contact> contacts = null;
    private ProgressDialog progressDialog = null;
    private ContactAdapter contactAdapter = null;
    private Runnable viewContacts = null;
    private SparseBooleanArray selectedContacts =   new SparseBooleanArray()  ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main2);
        contacts = new ArrayList<>();
        this.contactAdapter = new ContactAdapter(this, R.layout.activity_contacts, contacts);
        setListAdapter(this.contactAdapter);

        viewContacts = new Runnable(){
            @Override
            public void run() {
                getContacts();
            }
        };
        Thread thread =  new Thread(null, viewContacts, "ContactReadBackground");
        thread.start();
        progressDialog = ProgressDialog.show(ContactsActivity.this,"Please wait...", "Retrieving contacts ...", true);
    }

    private void getContacts(){
        try{
            String[] projection = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER,
                    ContactsContract.Contacts._ID
            };

            Cursor cursor = managedQuery(ContactsContract.Contacts.CONTENT_URI, projection, ContactsContract.Contacts.HAS_PHONE_NUMBER+"=?", new String[]{"1"}, ContactsContract.Contacts.DISPLAY_NAME);

            contacts = new ArrayList<Contact>();
            while(cursor.moveToNext()){
                Contact contact = new Contact();
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                contact.setContactName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                contacts.add(contact);
            }
            cursor.close();
            runOnUiThread(returnRes);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
            // close the progress dialog
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            contactAdapter.notifyDataSetChanged();
        }
    };

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
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.activity_contacts, null);
            }
            Contact contact = items.get(position);
            if (contact != null) {
                CheckBox nameCheckBox = (CheckBox) view.findViewById(R.id.checkBox);
                nameCheckBox.setChecked(selectedContacts.get(position));
                if (nameCheckBox != null) {
                    nameCheckBox.setText(contact.getContactName());
                }
                nameCheckBox.setOnClickListener(new OnItemClickListener(position,nameCheckBox.getText(),nameCheckBox));
            }
            return view;
        }
    }

    class OnItemClickListener implements OnClickListener{
        private int position;
        private CharSequence text;
        private CheckBox checkBox;
        OnItemClickListener(int position, CharSequence text,CheckBox checkBox){
            this.position = position;
            this.text = text;
            this.checkBox = checkBox;
        }
        @Override
        public void onClick(View arg0) {
            selectedContacts.append(position, true);
            Toast.makeText(getBaseContext(), "Clicked "+position +" and text "+text,Toast.LENGTH_SHORT).show();
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
}