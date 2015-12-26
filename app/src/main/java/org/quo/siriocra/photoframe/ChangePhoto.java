package org.quo.siriocra.photoframe;

import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by siriocra on 25.12.15.
 */
public class ChangePhoto {
    private FullscreenActivity activity;
    private ImageView imageView;
    private List<Photo> photoList;
    private WebSync webSync;
    private Integer currentPhotoIndex;
    public Integer currentRevision;

    public ChangePhoto(FullscreenActivity activity) {
        this.activity = activity;
        imageView = (ImageView) activity.findViewById(R.id.image);
        webSync = null;
        currentPhotoIndex = null;
        currentRevision = null;
        photoList = null;
        getPhotoList();
        if (!photoList.isEmpty())
            setPhoto(0);
    }

    private void getPhotoList() {
        if (webSync == null) {
            webSync = new WebSync(activity, this);
        }
        if (webSync.checkRevision(currentRevision)) {
            photoList = webSync.getNewPhotos(currentRevision);
        } else {
            photoList = getOldPhotos();
        }
    }

    private List<Photo> getOldPhotos() {
        return StorageManager.loadPhotosFromStorage(activity);
    }

    private void setPhoto(Integer index) {
        currentPhotoIndex = index;
        Photo photo = photoList.get(index);
        imageView.setImageBitmap(BitmapFactory.decodeFile(photo.path));
    }

    public void changePhoto() {
        if (!photoList.isEmpty())
            setPhoto((currentPhotoIndex + 1) % photoList.size());
    }
}
