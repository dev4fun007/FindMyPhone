package findmyphone.sync.bytes.findmyphone.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by I319829 on 13-07-2017.
 */
public class Writer {

    private static final String TAG = Writer.class.getCanonicalName();

    public static void writePIN(Context context, String pin)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        sharedPref.edit().putString(Constants.PIN_KEY_NAME, pin).apply();
        Log.d(TAG, "PIN Saved: "+pin);
    }

    public static void writePinNotSet(Context context, boolean isPinNotSet)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(Constants.PIN_NOT_SET, isPinNotSet).apply();
        Log.d(TAG, "Pin not set Boolean Saved: "+isPinNotSet);
    }

    public static void writeSendLocation(Context context, boolean isSendLocationEnabled)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(Constants.SEND_LOCATION, isSendLocationEnabled).apply();
        Log.d(TAG, "Send Location Boolean Saved: "+isSendLocationEnabled);
    }

    public static void isFirstTime(Context context, boolean isFirstTime)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(Constants.IS_FIRST_TIME, isFirstTime).apply();
        Log.d(TAG, "isFirstTime Saved: "+isFirstTime);
    }
}
