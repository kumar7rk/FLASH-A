/*
* Let's you update the keyword; save it in sharePreferences; share with your family friends via SMS
*
* */
package com.geeky7.rohit.flash_a.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.geeky7.rohit.flash_a.CONSTANT;
import com.geeky7.rohit.flash_a.Main;
import com.geeky7.rohit.flash_a.R;

public class Keyword extends DialogFragment {
    SharedPreferences preferences;
    EditText keyword;
    AlertDialog.Builder alertDialog;
    Main m;

    public static final String TAG = CONSTANT.KEYWORD;
    public Keyword() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        m = new Main(getActivity());
        m.calledMethodLog(TAG,"onCreate");
        super.onCreate(savedInstanceState);
    }

    //sets up the dialog; pretty much does everything this class is expected too
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        m.calledMethodLog(TAG,"onCreateDialog");
        //setting up Shared Preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = preferences.edit();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_keyword, null);

        keyword= (EditText) view.findViewById(R.id.keyword_et);

        //putting the caret on the last of the keyword for convenience
        keyword.setSelection(keyword.getText().length());

        // creating dialog with buttons
        //on click save button save the keyword in the sharedPreferences
        alertDialog = new AlertDialog.Builder(getActivity())
        .setTitle("Edit Keyword")
        .setView(inflater.inflate(R.layout.activity_keyword, null))
                // save button, onClick updates the keyword in sharedPreference
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String keywordS = keyword.getText().toString().trim();
                        editor.putString(CONSTANT.KEYWORD,keywordS);
                        editor.apply();
                        Main.showToast("Keyword updated :)");
                    }
                })
                // close button to close the dialog
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
        //share button to share the keyword via sms
        // open the default messaging app with pre added text with keyword
        .setNeutralButton(R.string.share, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String keyword = preferences.getString(CONSTANT.KEYWORD,"");
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"));
                sendIntent.putExtra("sms_body", "Get my current location by messaging me the secret keyword " + "\"" + keyword+"\""+".Shhhh. This great app ASHA keeps us connected. http://bit.ly/get-asha");
                startActivity(sendIntent);
            }
        });
        return alertDialog.create();
    }

    //gets the saved keyword from the sharedPreferences and also magically puts the caret at the end of the text
    // yeah it's not like that by default
    @Override
    public void onResume() {
        m.calledMethodLog(TAG,"onResume");

        super.onResume();
        keyword = (EditText) getDialog().findViewById(R.id.keyword_et);
        // get the stored keyword
        keyword.setText(preferences.getString(CONSTANT.KEYWORD,"Asha"));
        // move the caret to the last of the text; front by default
        keyword.setSelection(keyword.getText().length());
    }
}