package org.quo.siriocra.photoframe;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 * Created by siriocra on 26.12.15.
 */
public class AutoUpdateApp extends AsyncTask <String, Integer, String> {
    private Activity activity;
    private String APK_NAME = "PhotoFrame.apk";

    public AutoUpdateApp(Activity activity) {
        this.activity = activity;
    }

    protected String doInBackground(String... sUrl) {
        String path = null;
        try {
            URL urlVersion = new URL(sUrl[0]);
            URLConnection connectionVersion = urlVersion.openConnection();
            connectionVersion.connect();
            InputStream inputVersion = new BufferedInputStream(urlVersion.openStream());
            String version = new Scanner(inputVersion).nextLine();
            if (FullscreenActivity.VERSION.equals(version)) {
                return null;
            }
            File newApk = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), APK_NAME);
            path = newApk.getPath();
            URL url = new URL(sUrl[1]);
            URLConnection connection = url.openConnection();
            connection.connect();

            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(path);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
            Log.d("downloadAppUpdate", path);
        } catch (Exception e) {
            Log.e("downloadAppUpdate", "Well that didn't work out so well...");
            Log.e("downloadAppUpdate", e.getMessage());
        }
        return path;
    }

    // begin the installation by opening the resulting file
    @Override
    protected void onPostExecute(String path) {
        if (path == null) {
            return;
        }
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
        Log.d("postDownloadAppUpdate", "About to install new .apk");
        activity.startActivity(i);
    }
}
