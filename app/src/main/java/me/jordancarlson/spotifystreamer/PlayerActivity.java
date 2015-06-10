package me.jordancarlson.spotifystreamer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.jordancarlson.spotifystreamer.adapters.TracksAdapter;
import me.jordancarlson.spotifystreamer.fragments.PlayerFragment;
import me.jordancarlson.spotifystreamer.models.ParcelableTrack;
import me.jordancarlson.spotifystreamer.utils.ToolbarUtil;


public class PlayerActivity extends AppCompatActivity implements PlayerFragment.OnFragmentInteractionListener{

    @InjectView(R.id.playerArtistTextView) TextView mArtistTextView;
    @InjectView(R.id.playerAlbumTextView) TextView mAlbumTextView;
    @InjectView(R.id.playerAlbumImageView) ImageView mAlbumImageView;
    @InjectView(R.id.playerTrackTextView) TextView mTrackTextView;
    @InjectView(R.id.playButton) ImageView mPlay;
    @InjectView(R.id.pauseButton) ImageView mPause;
    @InjectView(R.id.seekBar) SeekBar mSeekBar;
    @InjectView(R.id.playerTimeRemaining) TextView mTimeRemainingTextView;
    @InjectView(R.id.playerTimeElapsed) TextView mTimeElapsedTextView;
    private MediaPlayer mMediaPlayer;
    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.inject(this);
        Intent intent = getIntent();

        // check if args are empty

            ToolbarUtil.setupToolbar(this, "Test Artist", null, false);

            mArtistTextView.setText(intent.getStringExtra(TracksAdapter.ARTIST_NAME));
            mAlbumTextView.setText(intent.getStringExtra(TracksAdapter.ALBUM_NAME));
            mTrackTextView.setText(intent.getStringExtra(TracksAdapter.TRACK_NAME));

            if (!TextUtils.isEmpty(intent.getStringExtra(TracksAdapter.ALBUM_IMAGE))) {
                Picasso.with(this)
                        .load(intent.getStringExtra(TracksAdapter.ALBUM_IMAGE))
                        .fit()
                        .centerCrop()
                        .into(mAlbumImageView);
            }

            String trackUrl = intent.getStringExtra(TracksAdapter.TRACK_URL);
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int position, boolean isFromUser) {
                    if (isFromUser) {
                        mMediaPlayer.seekTo(position);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(trackUrl);
                mMediaPlayer.prepareAsync();
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(getString(R.string.progress_dialog_message));
                progressDialog.show();
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        progressDialog.dismiss();
                        mMediaPlayer.start();
                        mPlay.setVisibility(View.GONE);
                        mPause.setVisibility(View.VISIBLE);
                        mSeekBar.setMax(mMediaPlayer.getDuration());
                        updateSeekBar();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    final Runnable run = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
        }
    };

    public void updateSeekBar() {
        mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
        mHandler.postDelayed(run, 10);
        String elapsed = formatElapsed(mMediaPlayer.getCurrentPosition());
        String remaining = formatRemaining(mMediaPlayer.getDuration(), mMediaPlayer.getCurrentPosition());
        mTimeElapsedTextView.setText(elapsed);
        mTimeRemainingTextView.setText(remaining);
    }

    private String formatRemaining(int duration, int current) {
        int remaining = (duration - current);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(remaining);
        return String.format("-%d:%02d", minutes, seconds);
    }

    public static String formatElapsed(long mil) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(mil);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(mil);
        return String.format("%d:%02d", minutes, seconds);
    }

    @OnClick(R.id.playButton) public void onPlayClicked() {
        mMediaPlayer.start();
        mPlay.setVisibility(View.GONE);
        mPause.setVisibility(View.VISIBLE);
    }
    @OnClick(R.id.pauseButton) public void onPauseClicked() {
        mMediaPlayer.pause();
        mPlay.setVisibility(View.VISIBLE);
        mPause.setVisibility(View.GONE);
    }

    @Override
    public void onFragmentHidden(ParcelableTrack[] tracks, int position, int seek) {

    }
}
