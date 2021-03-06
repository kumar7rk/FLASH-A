// This class receives an incoming message, put the content in a intent and sends it to the backgroundService
// Along with that from 2q12 the receiver would also start when a device completes boot--> that's dope
package com.geeky7.rohit.flash_a;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.geeky7.rohit.flash_a.services.BackgroundService;

/**
 * Created by Rohit on 8/07/2016.
 */
public class SMSReceiver extends BroadcastReceiver {

    public static final String TAG = CONSTANT.SMS_RECEIVER;
    Main m;
    public void onReceive(Context context, Intent intent) {
        m = new Main(MyApplication.getAppContext());
        m.calledMethodLog(TAG,"onReceive");
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
                    senderNum = currentMessage.getDisplayOriginatingAddress();
                    message = currentMessage.getDisplayMessageBody();
                    m.updateLog("SmsReceiver", "Sender: " + senderNum + "; Message: " + message);
                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);
        }

        // starting the background service with the message content and sender number
        Intent intent1 = new Intent(context,BackgroundService.class);
        intent1.putExtra("Message", message);
        intent1.putExtra(CONSTANT.SENDER, senderNum);
        context.startService(intent1);
    }
}
