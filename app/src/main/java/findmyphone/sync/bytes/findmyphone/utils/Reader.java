package findmyphone.sync.bytes.findmyphone.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by I319829 on 13-07-2017.
 */
public class Reader {


    public static String readPIN(Context context)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPref.getString(Constants.PIN_KEY_NAME, null);
    }

    public static boolean readSendLocation(Context context)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(Constants.SEND_LOCATION, false);
    }

    public static boolean readIsFirstTime(Context context)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(Constants.IS_FIRST_TIME, true);
    }

}
