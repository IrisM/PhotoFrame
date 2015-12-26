package org.quo.siriocra.photoframe;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by siriocra on 26.12.15.
 */
public class StorageManager {
    static public String PHOTO_DIRECTORY = "Photos";

    // Returns ablsolute path of stored image
    static public String saveToInternalStorage(Activity activity, String path, String name, Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(activity.getApplicationContext());
        File directory = cw.getDir(PHOTO_DIRECTORY + path, Context.MODE_PRIVATE);
        File stored_path = new File(directory, name);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(stored_path);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath() + "/" + name;
    }

    static public Bitmap loadImageFromStorage(Activity activity, String path, String name)
    {
        try {
            ContextWrapper cw = new ContextWrapper(activity.getApplicationContext());
            File directory = cw.getDir(PHOTO_DIRECTORY + path, Context.MODE_PRIVATE);
            File f = new File(directory, name);
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    static public List<Photo> loadPhotosFromStorage(Activity activity)
    {
        List<Photo> photos = new ArrayList<>();
        ContextWrapper cw = new ContextWrapper(activity.getApplicationContext());
        File directory = cw.getDir(PHOTO_DIRECTORY, Context.MODE_PRIVATE);
        for (File album : directory.listFiles()) {
            if (album.isDirectory()) {
                for (File photo : album.listFiles()) {
                    photos.add(new Photo(photo.getPath(), photo.getName()));
                }
            } else {
                photos.add(new Photo(album.getPath(), album.getName()));
            }
        }
        return photos;
    }
}
