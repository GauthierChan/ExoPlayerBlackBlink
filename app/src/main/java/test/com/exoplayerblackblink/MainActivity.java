package test.com.exoplayerblackblink;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.image_video_long) ImageView image_video_long;
    @BindView(R.id.image_video_short) ImageView image_video_short;


    // ----------------------------------------------------------------------------------- LIFECYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        GlideHelper.loadThumbnail(image_video_long, R.drawable.long_preview, null);
        GlideHelper.loadThumbnail(image_video_short, R.drawable.short_preview, null);

        image_video_long.setTransitionName(Constants.VIEW_LONG_TRANSITION_NAME);
        image_video_short.setTransitionName(Constants.VIEW_SHORT_TRANSITION_NAME);
    }


    // -------------------------------------------------------------------------------------- CLICKS

    @OnClick(R.id.image_video_long)
    public void onImageVideoLongClick(View v){

        navigateToDetailActivityWithSharedElementTransition(v, Constants.VIDEO_IDENTIFIER_LONG);
    }

    @OnClick(R.id.image_video_short)
    public void onImageVideoShortClick(View v){

        navigateToDetailActivityWithSharedElementTransition(v, Constants.VIDEO_IDENTIFIER_SHORT);
    }

    // ------------------------------------------------------------------------------------- PRIVATE

    private void navigateToDetailActivityWithSharedElementTransition(@NonNull View viewToAnimate, int videoIdentifier){

        Log.w(TAG, "navigateToDetailActivityWithSharedElementTransition. videoIdentifier: "+videoIdentifier);

        Intent imageIntent = new Intent(this, DetailActivity.class);
        imageIntent.putExtra(Constants.EXTRA_VIDEO_TRANSITION_NAME, viewToAnimate.getTransitionName());
        imageIntent.putExtra(Constants.EXTRA_VIDEO_IDENTIFIER, videoIdentifier);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                viewToAnimate,
                viewToAnimate.getTransitionName());

        startActivity(imageIntent, options.toBundle());

    }
}
