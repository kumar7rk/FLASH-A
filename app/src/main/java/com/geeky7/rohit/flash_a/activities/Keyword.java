package com.geeky7.rohit.flash_a.activities;

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

    public Keyword() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = preferences.edit();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_keyword, null);

        keyword= (EditText) view.findViewById(R.id.keyword_et);
        keyword.setSelection(keyword.getText().length());

        alertDialog = new AlertDialog.Builder(getActivity())
        .setTitle("Edit Keyword")
        .setView(inflater.inflate(R.layout.activity_keyword, null))
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String keywordS = keyword.getText().toString();
                        editor.putString(CONSTANT.KEYWORD,keywordS);
                        editor.apply();
                        Main.showToast("Keyword updated :)");
                    }
                })
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
        .setNeutralButton(R.string.share, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                \"Asha"
                String keyword = preferences.getString(CONSTANT.KEYWORD,"");
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"));
                sendIntent.putExtra("sms_body", "Get my current location by messaging me the secret keyword" + "\"" + keyword+"\""+". This great app ASHA keeps us connected. http://bit.ly/get-asha");
                startActivity(sendIntent);
            }
        });
        return alertDialog.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        keyword = (EditText) getDialog().findViewById(R.id.keyword_et);
        // get the stored keyword
        keyword.setText(preferences.getString(CONSTANT.KEYWORD,"Asha"));
        // move the caret to the last of the text; front by default
        keyword.setSelection(keyword.getText().length());
    }
}