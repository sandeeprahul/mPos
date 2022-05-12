package in.hng.mpos.helper;

/**
 * Created by Cbly on 27-Mar-18.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.Environment;

import com.crashlytics.android.Crashlytics;

public class Log {
    private static final String NEW_LINE = System.getProperty("line.separator");
    public static boolean mLogcatAppender = true;
    static File mLogFile;

    /*static {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);

        File logfile = Environment.getExternalStorageDirectory();
        File myFile = new File(logfile.getAbsolutePath() + "/mPOSlog/");

        if (!myFile.exists()) {
            myFile.mkdir();
        }


        mLogFile = new File(myFile, formattedDate+".log");
        if (!mLogFile.exists()) {
            try {
                mLogFile.createNewFile();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
       // logDeviceInfo();
    }*/

    public static void i(String TAG, String message) {

        appendLog(TAG + " : " + message);
        if (mLogcatAppender) {
            android.util.Log.i(TAG, message);
        }
    }

    public static void d(String TAG, String message) {
        appendLog(TAG + " : " + message);
        if (mLogcatAppender) {
            android.util.Log.d(TAG, message);
        }
    }

    public static void e(String TAG, String message) {
        appendLog(TAG + " : " + message);
        if (mLogcatAppender) {
            android.util.Log.e(TAG, message);
        }
    }

    public static void v(String TAG, String message) {
        appendLog(TAG + " : " + message);
        if (mLogcatAppender) {
            android.util.Log.v(TAG, message);
        }
    }

    public static void w(String TAG, String message) {
        appendLog(TAG + " : " + message);
        if (mLogcatAppender) {
            android.util.Log.w(TAG, message);
        }
    }

    private static synchronized void appendLogTemp(String text) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Crashlytics.log(text);
        try {
            final FileWriter fileOut = new FileWriter(mLogFile, true);
            fileOut.append(sdf.format(new Date()) + " : " + text + NEW_LINE);
            fileOut.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized void appendLog(String text) {

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd_MM_yyyy_HH_mm");
        String formattedDate = df.format(c);

        File logfile = Environment.getExternalStorageDirectory();
//        File myFile = new File(logfile.getAbsolutePath());
        File myFile = new File(logfile.getAbsolutePath() +File.separator+ "mPOSlog/");
        if (!myFile.exists()) {
            myFile.mkdir();
        }


        mLogFile = new File(myFile, "mPOSlog"+formattedDate + ".log");
        if (!mLogFile.exists()) {
            try {
                mLogFile.createNewFile();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        final SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm");
        //Crashlytics.log(text);
        try {
            final FileWriter fileOut = new FileWriter(mLogFile, true);
            fileOut.append(sdf.format(new Date()) + " : " + text + NEW_LINE);
            fileOut.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }


    private static void logDeviceInfo() {
        appendLog("Model : " + android.os.Build.MODEL);
        appendLog("Brand : " + android.os.Build.BRAND);
        appendLog("Product : " + android.os.Build.PRODUCT);
        appendLog("Device : " + android.os.Build.DEVICE);
        appendLog("Codename : " + android.os.Build.VERSION.CODENAME);
        appendLog("Release : " + android.os.Build.VERSION.RELEASE);
    }
}
