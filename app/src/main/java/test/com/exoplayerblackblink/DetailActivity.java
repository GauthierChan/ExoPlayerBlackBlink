package test.com.exoplayerblackblink;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.ChangeClipBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import butterknife.BindView;
import butterknife.ButterKnife;

import static test.com.exoplayerblackblink.Constants.VIDEO_IDENTIFIER_LONG;

/**
 * Created by gauthier on 19/02/2018.
 */

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";

    @IntDef({TransitionDuration.SHORT, TransitionDuration.LONG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TransitionDuration {
        int SHORT = 300;
        int LONG = 600;
    }

    @BindView(R.id.image_preview) ImageView vImagePreview;
    @BindView(R.id.exoplayer_view) com.google.android.exoplayer2.ui.SimpleExoPlayerView vExoplayer;
    @BindView(R.id.progress_bar) ProgressBar vProgressBar;
    @BindView(R.id.toolbar) Toolbar vToolbar;

    protected boolean isFinishing; // is the Activity finishing? This is used when the SharedElementTransition finishes: has the animation ended because the Activity is created or finished?

    private int videoIdentifier;
    private SimpleExoPlayer mPlayer;


    // ----------------------------------------------------------------------------------- LIFECYCLE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        postponeEnterTransition();

        ButterKnife.bind(this);

        // Make the SharedElementTransition a little faster. This will also make the Activity listen to the onTransitionEnd of the SharedElementTransition
        getWindow().setSharedElementEnterTransition(getCustomDurationActivityEnterSharedElementTransition(TransitionDuration.SHORT));
        getWindow().setSharedElementReturnTransition(getCustomDurationActivityEnterSharedElementTransition(TransitionDuration.SHORT));

        // Prepare the SharedElementTransition
        String sharedElementTransitionName = getIntent().getStringExtra(Constants.EXTRA_VIDEO_TRANSITION_NAME);
        videoIdentifier = getIntent().getIntExtra(Constants.EXTRA_VIDEO_IDENTIFIER, 0);
        vImagePreview.setTransitionName(sharedElementTransitionName);
        GlideHelper.loadThumbnail(vImagePreview, videoIdentifier == VIDEO_IDENTIFIER_LONG ? R.drawable.long_preview : R.drawable.short_preview, new RequestListener<Object, Bitmap>() {
            @Override
            public boolean onException(Exception e, Object model, Target<Bitmap> target, boolean isFirstResource) {

                startPostponedEnterTransition();
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {

                startPostponedEnterTransition();
                return false;
            }
        });

        setSupportActionBar(vToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mPlayer != null){
            mPlayer.stop();
            mPlayer.release();
        }
    }

    // ----------------------------------------------------------------------------------- INTERFACE

    @Override
    public void finishAfterTransition() {

        vImagePreview.setVisibility(View.VISIBLE);
        vExoplayer.setVisibility(View.GONE);

        super.finishAfterTransition();
        isFinishing = true;
    }

    @Override
    public void onBackPressed() {

        if(isFinishing || !isFinishing()) {
            finishAfterTransition();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishAfterTransition();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    // ------------------------------------------------------------------------------------- PRIVATE

    private Transition getCustomDurationActivityEnterSharedElementTransition(@TransitionDuration int duration){
        TransitionSet set = new TransitionSet();
        set.addTransition(new ChangeBounds());
        set.addTransition(new ChangeTransform());
        set.addTransition(new ChangeClipBounds());
        set.addTransition(new ChangeImageTransform());
        set.setDuration(duration);
        set.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                Log.i(TAG, "onTransitionStart");
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                Log.i(TAG, "onTransitionEnd");

                // Load the video
                if(!isFinishing){
                    handleLoadingVideo();
                }
            }

            @Override
            public void onTransitionCancel(Transition transition) {
                Log.i(TAG, "onTransitionCancel");
            }

            @Override
            public void onTransitionPause(Transition transition) {
                Log.i(TAG, "onTransitionPause");
            }

            @Override
            public void onTransitionResume(Transition transition) {
                Log.i(TAG, "onTransitionResume");
            }
        });
        return set;
    }

    private void handleLoadingVideo(){
        vProgressBar.setVisibility(View.VISIBLE);

        mPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        mPlayer.setPlayWhenReady(false);
        mPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);

        vExoplayer.setPlayer(mPlayer);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)));

        Log.i(TAG, "Preparing source "+ videoIdentifier);
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(videoIdentifier == Constants.VIDEO_IDENTIFIER_LONG ? Uri.parse("file:///android_asset/long.mp4") : Uri.parse("file:///android_asset/short.mp4"));

        LoopingMediaSource loopingSource = new LoopingMediaSource(mediaSource);
        mPlayer.prepare(mediaSource);
        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

                Log.i(TAG, "onTimelineChanged");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

                Log.i(TAG, "onTracksChanged");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

                Log.i(TAG, "onLoadingChanged "+isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                Log.i(TAG, "onPlayerStateChanged "+ playbackState);
                switch(playbackState){
                    case Player.STATE_READY:

                        vExoplayer.setVisibility(View.VISIBLE);
                        vImagePreview.setVisibility(View.INVISIBLE);
                        vExoplayer.showController();

                        vProgressBar.setVisibility(View.GONE);


                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

                Log.i(TAG, "onRepeatModeChanged "+repeatMode);
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

                Log.i(TAG, "onShuffleModeEnabledChanged "+shuffleModeEnabled);
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.i(TAG, "onPlayerError ");
                error.printStackTrace();
            }

            @Override
            public void onPositionDiscontinuity(int reason) {

                Log.i(TAG, "onPositionDiscontinuity "+reason);
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

                Log.i(TAG, "onPlaybackParametersChanged");
            }

            @Override
            public void onSeekProcessed() {

                Log.i(TAG, "onSeekProcessed");
            }
        });

    }

}
