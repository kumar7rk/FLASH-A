package com.geeky7.rohit.flash_a.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
        alertDialog = new AlertDialog.Builder(getActivity());

        alertDialog.setTitle("Edit Keyword");

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.activity_keyword, null);
        keyword= (EditText) view.findViewById(R.id.keyword_et);

        keyword.setSelection(keyword.getText().length());

        alertDialog.setView(inflater.inflate(R.layout.activity_keyword, null))

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
                });
        return alertDialog.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        keyword = (EditText) getDialog().findViewById(R.id.keyword_et);
        keyword.setText(preferences.getString(CONSTANT.KEYWORD,"Asha"));
        keyword.setSelection(keyword.getText().length());
    }

}