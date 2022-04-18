package in.hng.mpos.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class AppLog {
    /*This method is used to show the toast messages*/
    public static void showToastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

    }

    /*This method is used to show the logs to logcat-console*/
    public static void showDebugLog(String logCatTag, String logMessage) {
        Log.d(logCatTag, logMessage);
    }

}

