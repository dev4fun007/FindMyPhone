package findmyphone.sync.bytes.findmyphone.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import findmyphone.sync.bytes.findmyphone.RingService;
import findmyphone.sync.bytes.findmyphone.utils.Reader;
import findmyphone.sync.bytes.findmyphone.utils.Writer;

public class IncomingSmsReceiver extends BroadcastReceiver {

    public static final String TAG = IncomingSmsReceiver.class.getCanonicalName();

//    private FusedLocationProviderClient fusedLocationProviderClient;

    public IncomingSmsReceiver() {
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "SMS received");

        //check for the set pin
        String pin = Reader.readPIN(context);
        if(pin == null)
        {
            Log.d(TAG, "Pin not set, tell user to set the pin");
            Writer.writePinNotSet(context, true);
            return;
        }

        //Everything is fine, check the sms for the keyword
        shouldRing(pin, intent, context);
    }


    private boolean shouldRing(String pin, Intent intent, Context context)
    {
        boolean ring = false;
        String  incomingNumber = "";

        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            //Log.d(TAG,""+bundle.);
            if (bundle != null) {

                //PDU-- portable document unit, apparently this is used as sms messages unit to send/receive sms
                Object[] pdus = (Object[]) bundle.get("pdus");
                String format = bundle.getString("format");

                if (pdus != null) {
                    final SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);    //New method for android m onwards
                        else
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);            //Old method to create sms from PDUs
                    }

                    //Validate the message
                    if (messages.length > -1) {
                        String msg = messages[0].getMessageBody();
                        incomingNumber = messages[0].getOriginatingAddress();
                        if (msg.trim().startsWith(pin) && (msg.trim().endsWith("Ring") || msg.trim().endsWith("ring") || msg.trim().endsWith("find") || msg.trim().endsWith("Find")))
                            ring = true;
                        Log.d(TAG, "SMS received: " + msg+ " Number: "+incomingNumber);
                    }
                }
            }

            if(ring)
            {
                Intent ringServiceIntent = new Intent(context, RingService.class);
                ringServiceIntent.setAction("SMS_RECEIVED_RING_SERVICE");
                ringServiceIntent.putExtra("INCOMING_NUMBER", incomingNumber);
                context.startService(ringServiceIntent);
            }

        }

        return ring;
    }



    /*private void ringThePhone(Context context)
    {
        //Get current ringtone
        Uri currentRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
        Ringtone ringtone = RingtoneManager.getRingtone(context, currentRingtoneUri);

        //Set max ringtone volume
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
        Log.d(TAG, "Starting to ring the phone");
        //Now sound the alarm
        ringtone.play();
    }


    private void sendLastLocation(Context context, final String incomingNumber)
    {
        Log.d(TAG, "Fetching last location");

        //Using the fused location provider
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null)
                {
                    //Create Google Maps url
                    String loc = "https://www.google.com/maps/search/?api=1&query="+location.getLatitude()+","+location.getLongitude();
                    //Send the location SMS
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(incomingNumber,null,loc, null, null);
                    Log.d(TAG, "SMS sent to the number: "+incomingNumber+" Message: "+loc);
                }
            }
        });
    }*/

}
