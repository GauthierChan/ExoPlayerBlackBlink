package test.com.exoplayerblackblink;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;

/**
 * Created by gauthier on 20/02/2018.
 */

public class GlideHelper {


    public static void loadThumbnail(ImageView imageView, int drawableId, RequestListener<Object, Bitmap> requestListener){

        Glide.with(imageView.getContext())
                .load(drawableId)
                .asBitmap()
                .override(300, 300) // TODO Forcing the size will make us use the cached version when we load a drawable for the second time. this will make the SharedElementTransition faster as we'll always load a cached version of the thumbnail
                .fitCenter()
                .listener(requestListener)
                .into(imageView);
    }
}
