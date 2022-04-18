package in.hng.mpos.Utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;

public class DownloadUtils {

    private static final String TAG = "DownloadUtils";


    //Downloader 
    private DownloadManager downloadManager;
    //Context
    private Context mContext;
    //Downloaded ID
    private long downloadId;

    public DownloadUtils(Context context) {
        this.mContext = context;
    }


    public void downloadApk(String downLoadUrl, String description) {

        String apkName = downLoadUrl.substring(downLoadUrl.lastIndexOf("/") + 1);

        Log.d(TAG, "DownLoadUrl: %s \nDownLoadDescription: \n%s" + downLoadUrl + "\n" + description);
        DownloadManager.Request request;
        try {
            request = new DownloadManager.Request(Uri.parse(downLoadUrl));
        } catch (Exception e) {
            Log.d(TAG, "downLoad failed :%s" + e.getLocalizedMessage());
            return;
        }

        request.setTitle("Download the title content of Notification");
        request.setDescription(description);

        // Show the download progress in the notification bar
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Set save download apk save path
        request.setDestinationInExternalPublicDir(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/", apkName);

        request.setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(downLoadUrl)));

        // Get DownloadManager
        downloadManager = (DownloadManager) mContext.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);

        //Add the download request to the download queue. After joining the download queue, it will return a long id to the task. With this id, you can cancel the task, restart the task, get the downloaded file, and so on.
        downloadId = downloadManager.enqueue(request);

        // Register the broadcast receiver, listen to the download status
        mContext.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    // Broadcast monitor the status of the download
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                Uri downloadFileUri = downloadManager.getUriForDownloadedFile(downloadId);
                installApk(downloadFileUri, context);
            }
        }
    };


    private String getRealFilePath(Context context, Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


    private void installApk(Uri downLoadApkUri, Context context) {

        if (downLoadApkUri == null) {
            Log.e(TAG, "Download apk failed,empty apk uri");
            return;
        } else {
            Log.d(TAG, "Download apk finish ,apkUri:%s" + downLoadApkUri.toString());
        }

        // Get the apk file to be installed
        File apkFile = new File(getRealFilePath(context, downLoadApkUri));
        if (!apkFile.exists()) {
            Log.d(TAG, "Apk file is not exist.");
            return;
        }

        // Call the system to install apk
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // version 7.0 or higher
            Uri uriForFile = FileProvider.getUriForFile(context, "com.example.app.fileProvider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(uriForFile, "application/vnd.android.package-archive");
            Log.d(TAG, "Install apk,\ndata: %s" + uriForFile);
        } else {
            Uri uri = Uri.fromFile(apkFile);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            Log.d(TAG, "Install apk,\ndata: %s" + uri);
        }

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Start system install activity exception: %s" + e.getLocalizedMessage());
        }

    }
}