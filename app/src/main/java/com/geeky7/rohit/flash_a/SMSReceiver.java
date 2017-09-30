// This class receives an incoming message, put the content in a intent and sends it to the backgroundService
package com.geeky7.rohit.flash_a;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.geeky7.rohit.flash_a.activities.MainActivity;
import com.geeky7.rohit.flash_a.services.BackgroundService;

/**
 * Created by Rohit on 8/07/2016.
 */
public class SMSReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        String message = "";
        String senderNum = "";
        // receives the message content in an intent
        final Bundle bundle = intent.getExtras();

        try {
        // check if the bundle has some data and extract the message and the sender number magically
            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    senderNum = phoneNumber;
                    message = currentMessage.getDisplayMessageBody();
                    Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);
                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);
        }
        // this code could be removed; next time I have my phone less than 3 metres away from me
        // I would test the code and remove it
        intent.putExtra("Message", message);
        intent.putExtra("Sender", senderNum);
        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, MainActivity.class);
//        context.startActivity(intent);


        // starting the background service with the message content and sender number
        Intent intent1 = new Intent(context,BackgroundService.class);
        intent1.putExtra("Message", message);
        intent1.putExtra("Sender", senderNum);
        context.startService(intent1);
    }
}
