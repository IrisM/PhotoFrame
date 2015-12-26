package org.quo.siriocra.photoframe;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.JsonReader;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by siriocra on 26.12.15.
 */
public class WebSync {
    private String BASE_URL = "http://grand.citxx.ru";
    private Activity activity;
    private ChangePhoto changePhoto;

    public WebSync(Activity activity, ChangePhoto changePhoto) {
        this.activity = activity;
        this.changePhoto = changePhoto;
    }

    // Returns true, if where is a new revision
    public Boolean checkRevision(Integer oldRevision) {
        if (oldRevision == null) {
            oldRevision = 0;
        }
        String relativeURL = "/revision";
        byte[] allData = sendGetRequest(relativeURL);
        Log.d("checkRevision", String.valueOf(allData.length));
        InputStream in = new ByteArrayInputStream(allData);
        JsonReader reader = null;
        Integer newRevision = oldRevision;
        try {
            // Reading and parsing new revision
            reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "revision":
                        newRevision = reader.nextInt();
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newRevision > oldRevision;
    }

    public List<Photo> getNewPhotos(Integer oldRevision) {
        if (oldRevision == null) {
            oldRevision = 0;
        }
        String relativeURL = "/diff/" + oldRevision;
        byte[] allData = sendGetRequest(relativeURL);
        Log.d("getNewPhotos", String.valueOf(allData.length));
        InputStream in = new ByteArrayInputStream(allData);
        JsonReader reader = null;
        List<Photo> photos = new ArrayList<>();
        try {
            // Reading and parsing raw json
            reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            Integer newRevision = null;
            List<Album> albums = null;
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "new_revision":
                        newRevision = reader.nextInt();
                        break;
                    case "albums":
                        albums = parseAlbums(reader);
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();

            // Making it a list of Photos
            if (albums == null)
                return null;
            if (newRevision != null) {
                changePhoto.currentRevision = newRevision;
            }
            for (Album album : albums) {
                for (String photoName : album.photoNames) {
                    photos.add(getPhoto(album.id, photoName));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return photos;
    }

    private List<Album> parseAlbums(JsonReader reader) throws IOException {
        List<Album> albums = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            albums.add(parseAlbum(reader));
        }
        reader.endArray();
        return albums;
    }

    private Album parseAlbum(JsonReader reader) throws IOException {
        reader.beginObject();
        Album album = new Album();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "id":
                    album.id = reader.nextString();
                    break;
                case "revision":
                    album.revision = reader.nextInt();
                    break;
                case "photo_ids":
                    album.photoNames = new ArrayList<>();
                    reader.beginArray();
                    while (reader.hasNext()) {
                        album.photoNames.add(reader.nextString());
                    }
                    reader.endArray();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return album;

    }

    private Photo getPhoto(String directory, String photoName) {
        String relativeURL = "/photo/" + photoName;
        byte[] allData = sendGetRequest(relativeURL);
        Log.d("getPhoto", String.valueOf(allData.length));
        InputStream in = new ByteArrayInputStream(allData);
        //TODO: check it
        Bitmap image = BitmapFactory.decodeStream(in);
        Log.d("getPhoto", "width: " + image.getWidth() + " height: " + image.getHeight());
        String path = StorageManager.saveToInternalStorage(activity, directory, photoName, image);
        return new Photo(path, photoName);
    }

    private byte[] sendGetRequest(String relativeURL) {
        HttpURLConnection connection = null;
        try {
            //Create connection
            URL url = new URL(BASE_URL + relativeURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            //Get Response in bytes
            InputStream in = connection.getInputStream();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[0xFFFF];
            for (int len; (len = in.read(buffer)) != -1;)
                os.write(buffer, 0, len);
            os.flush();
            byte[] response = os.toByteArray();

            Log.d("sendGetRequest", url.toString() + " " + response.length);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}