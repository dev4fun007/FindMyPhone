package findmyphone.sync.bytes.findmyphone;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Timer;
import java.util.TimerTask;

import findmyphone.sync.bytes.findmyphone.receivers.IncomingSmsReceiver;
import findmyphone.sync.bytes.findmyphone.utils.Reader;

public class RingService extends Service {

    public static final String TAG = RingService.class.getCanonicalName();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private AudioManager audioManager;
    private Ringtone ringtone;

    private Vibrator vibrator;
    private Timer vibrateTimer;
    protected static final long VIBRATE_DURATION = 200l;
    protected static final long VIBRATE_WAIT_DURATION = 2000l;

    private CameraManager mCameraManager;
    private String mCameraId;
    private Timer flashTimer;
    protected static final long FLASH_ON_DURATION = 50l;
    protected static final long FLASH_WAIT_DURATION = 200l;

    public RingService() {
    }


    @Override
    public void onCreate()
    {
        super.onCreate();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        vibrateTimer = new Timer();
        flashTimer = new Timer();

        //Flashlight feature
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            Log.d(TAG, "Error fetching camera", e);
        }

    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if(vibrateTimer != null)
        {
            vibrateTimer.cancel();
            vibrateTimer.purge();
        }

        if(flashTimer != null)
        {
            flashTimer.cancel();
            flashTimer.purge();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(intent!=null) {
            if("SMS_RECEIVED_RING_SERVICE".equals(intent.getAction()))
            {
                String incomingNumber = intent.getStringExtra("INCOMING_NUMBER");
                ringThePhone(this);
                Log.d(TAG, "Phone should be ringing now");
                if (Reader.readSendLocation(this)) {
                    sendLastLocation(this, incomingNumber);
                    Log.d(TAG, "Last known location sent");
                }
            }
            else if("STOP_RINGING".equals(intent.getAction()))
            {
                //Stop the ringing
                if(ringtone.isPlaying())
                    ringtone.stop();

                //Stop timer tasks
                if(vibrateTimer != null)
                {
                    Log.d(TAG,"Cancel and purge vibrate timer tasks");
                    vibrateTimer.cancel();
                    vibrateTimer.purge();
                }

                if(flashTimer != null)
                {
                    Log.d(TAG,"Cancel and purge flash timer tasks");
                    flashTimer.cancel();
                    flashTimer.purge();
                }

                try {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        mCameraManager.setTorchMode(mCameraId, false);
                }
                catch (Exception e)
                {
                    Log.d(TAG, "", e);
                }
                stopSelf();
            }
        }

        showNotification();

        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private void ringThePhone(Context context)
    {
        //Get current ringtone
        Uri currentRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(context, currentRingtoneUri);

        //Set max ringtone volume
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
        Log.d(TAG, "Starting to ring the phone");
        //Now sound the alarm
        ringtone.play();

        //Also vibrate the phone
        if(vibrator != null)
        {
            Log.d(TAG, "Going to vibrate the device");
            VibrateTimerTask task = new VibrateTimerTask(vibrator);
            vibrateTimer.schedule(task, 3000, 2000);
        }

        //Flashlight support
        if(mCameraManager != null)
        {
            Log.d(TAG, "Starting flashlight, signal");
            FlashTimerTask task = new FlashTimerTask(mCameraManager, mCameraId);
            flashTimer.schedule(task, 3000, 2000);
        }

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
    }


    private void showNotification()
    {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction("STOP_RINGING");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent cancelIntent = new Intent(this, RingService.class);
        cancelIntent.setAction("STOP_RINGING");
        PendingIntent cancelPendingIntent = PendingIntent.getService(this, 0, cancelIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        Notification notification = new NotificationCompat.Builder(this, "DEFAULT_CHANNEL")
                .setContentTitle("Find My Phone")
                .setTicker("Ringing the phone")
                .setContentText("Press cancel to stop ringing")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_delete, "Stop Ringing", cancelPendingIntent)
                .build();

        startForeground(1, notification);
    }

}



class VibrateTimerTask extends TimerTask
{

    private static final String TAG = VibrateTimerTask.class.getCanonicalName();
    private Vibrator vibrator;

    public VibrateTimerTask(Vibrator vibrator)
    {
        this.vibrator = vibrator;
    }


    @Override
    public void run()
    {
        Log.d(TAG,"Vibrating the device");
       vibrator.vibrate(1000);
    }
}


class FlashTimerTask extends TimerTask
{
    private static final String TAG = VibrateTimerTask.class.getCanonicalName();
    private CameraManager mCameraManager;
    private boolean isFlashOn = false;
    private String mCameraId;


    public FlashTimerTask(CameraManager mCameraManager, String mCameraId)
    {
        this.mCameraManager = mCameraManager;
        this.mCameraId = mCameraId;
    }


    @Override
    public void run()
    {
        Log.d(TAG,"Flashing the flash light");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG,"Android Version >= M");
                isFlashOn = !isFlashOn;
                mCameraManager.setTorchMode(mCameraId, isFlashOn);
            }
        } catch (Exception e) {
            Log.d(TAG, "Cannot set the flash to torch mode");
        }

    }
}
