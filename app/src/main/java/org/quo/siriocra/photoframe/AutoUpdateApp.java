package org.quo.siriocra.photoframe;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class AutoUpdateApp implements Runnable {
    private Activity activity;
    private URL versionUrl;
    private URL apkUrl;
    private File apkFile;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public void verifyStoragePermission() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public AutoUpdateApp(Activity activity, String versionUrl, String apkUrl, File apkFile) throws MalformedURLException {
        this.activity = activity;
        this.versionUrl = new URL(versionUrl);
        this.apkUrl = new URL(apkUrl);
        this.apkFile = apkFile;
    }

    public void run() {
        String path = null;
        try {
            URLConnection connectionVersion = versionUrl.openConnection();
            connectionVersion.connect();
            InputStream inputVersion = new BufferedInputStream(versionUrl.openStream());
            String version = new Scanner(inputVersion).nextLine();
            if (FullscreenActivity.VERSION.equals(version)) {
                return;
            }
            path = apkFile.getPath();
            URLConnection connection = apkUrl.openConnection();
            connection.connect();

            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(apkUrl.openStream());
            OutputStream output = new FileOutputStream(path);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
//                publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
            Log.d("downloadAppUpdate", path);

            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
            Log.d("postDownloadAppUpdate", "About to install new .apk");
            activity.startActivity(i);
        } catch (IOException e) {
            Log.e("downloadAppUpdate", "Caught IOException");
            Log.e("downloadAppUpdate", e.getMessage());
        }
    }
}
