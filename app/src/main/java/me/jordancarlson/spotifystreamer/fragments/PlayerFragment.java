package me.jordancarlson.spotifystreamer.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.jordancarlson.spotifystreamer.R;
import me.jordancarlson.spotifystreamer.models.ParcelableTrack;
import me.jordancarlson.spotifystreamer.utils.MediaPlayerSingleton;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerFragment extends DialogFragment {

    private static final String ARTIST_NAME = "artistName";
    private static final String ALBUM_NAME = "albumName";
    private static final String TRACK_NAME = "trackName";
    private static final String ALBUM_IMAGE = "albumImage";
    private static final String TRACK_URL = "trackUrl";
    private static final String TRACKS = "tracks";
    private static final String POSITION = "position";
    private static final String SEEK = "seek";

    @InjectView(R.id.playerArtistTextView) TextView mArtistTextView;
    @InjectView(R.id.playerAlbumTextView) TextView mAlbumTextView;
    @InjectView(R.id.playerAlbumImageView) ImageView mAlbumImageView;
    @InjectView(R.id.playerTrackTextView) TextView mTrackTextView;
    @InjectView(R.id.playButton) ImageView mPlay;
    @InjectView(R.id.pauseButton) ImageView mPause;
    @InjectView(R.id.seekBar) SeekBar mSeekBar;
    @InjectView(R.id.playerTimeRemaining) TextView mTimeRemainingTextView;
    @InjectView(R.id.playerTimeElapsed) TextView mTimeElapsedTextView;
    @InjectView(R.id.player_toolbar) Toolbar mToolbar;
    @InjectView(R.id.player_root_layout) RelativeLayout mRootLayout;
    private MediaPlayer mMediaPlayer;
    private final Handler mHandler = new Handler();


    private boolean mHideToolbar;

    private OnFragmentInteractionListener mListener;
    private ParcelableTrack[] mTracks;
    private int mPosition;
    private int mSeek;

    public static PlayerFragment newInstance(ParcelableTrack[] tracks, int position, int seek) {
    PlayerFragment fragment = new PlayerFragment();
    Bundle args = new Bundle();
    ParcelableTrack track = tracks[position];
    args.putString(ARTIST_NAME, track.getArtistName());
    args.putString(ALBUM_NAME, track.getAlbumName());
    args.putString(TRACK_NAME, track.getTrackName());
    args.putString(TRACK_URL, track.getTrackUrl());
    args.putString(ALBUM_IMAGE, track.getAlbumImage());
    args.putParcelableArray(TRACKS, tracks);
    args.putInt(SEEK, seek);
    args.putInt(POSITION, position);
    fragment.setArguments(args);
    return fragment;
}

    public PlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String artistName = getArguments().getString(ARTIST_NAME);
            String albumName = getArguments().getString(ALBUM_NAME);
            String trackName = getArguments().getString(TRACK_NAME);
            String trackUrl = getArguments().getString(TRACK_URL);
            String albumImage = getArguments().getString(ALBUM_IMAGE);
            Parcelable[] parcelableArray = getArguments().getParcelableArray(TRACKS);
            mTracks = Arrays.copyOf(parcelableArray, parcelableArray.length, ParcelableTrack[].class);
            mPosition = getArguments().getInt(POSITION);
            mSeek = getArguments().getInt(SEEK);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.inject(this, view);

        // Hide Toolbar if tablet
        if (mHideToolbar) {
            mToolbar.setVisibility(View.GONE);
        } else {
            mRootLayout.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
            ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
            mToolbar.setTitle(getString(R.string.toolbar_title_search_activity));
            try {
                ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        playTrack(mTracks[mPosition]);

        return view;
    }

    private void playTrack(ParcelableTrack track) {
        mArtistTextView.setText(track.getArtistName());
        mAlbumTextView.setText(track.getAlbumName());
        mTrackTextView.setText(track.getTrackName());

        if (!TextUtils.isEmpty(track.getAlbumImage())) {
            Picasso.with(getActivity())
                    .load(track.getAlbumImage())
                    .fit()
                    .centerCrop()
                    .into(mAlbumImageView);
        }

        String trackUrl = track.getTrackUrl();
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
            mMediaPlayer = MediaPlayerSingleton.getInstance().getsMediaPlayer();
            mMediaPlayer.reset();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(trackUrl);
            mMediaPlayer.prepareAsync();
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.progress_dialog_message));
            progressDialog.show();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    progressDialog.dismiss();
                    if (mSeek > 0) {
                        mMediaPlayer.seekTo(mSeek);
                    }
                    mMediaPlayer.start();
                    mPlay.setVisibility(View.GONE);
                    mPause.setVisibility(View.VISIBLE);
                    mSeekBar.setMax(mMediaPlayer.getDuration());
                    updateSeekBar();
                }
            });
        } catch (Exception e) {
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
        if (mMediaPlayer.isPlaying()) {
            mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
            mSeek = mMediaPlayer.getCurrentPosition();
            mHandler.postDelayed(run, 10);
            String elapsed = formatElapsed(mMediaPlayer.getCurrentPosition());
            String remaining = formatRemaining(mMediaPlayer.getDuration(), mMediaPlayer.getCurrentPosition());
            mTimeElapsedTextView.setText(elapsed);
            mTimeRemainingTextView.setText(remaining);
        }
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
    @OnClick(R.id.previousButton) public void onPreviousClicked() {
        mSeek = 0;
        mPosition--;
        if (mPosition >= 0) {
            playTrack(mTracks[mPosition]);

        } else {
            mPosition = 9;
            playTrack(mTracks[mPosition]);
        }
    }
    @OnClick(R.id.nextButton) public void onNextClicked() {
        mSeek = 0;
        mPosition++;
        if (mPosition < mTracks.length) {
            playTrack(mTracks[mPosition]);

        } else {
            mPosition = 0;
            playTrack(mTracks[mPosition]);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mListener.onFragmentHidden(mTracks, mPosition, mSeek);
        mMediaPlayer.pause();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mHideToolbar = true;
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(ParcelableTrack[] tracks, int position, int seek) {
        if (mListener != null) {
            mListener.onFragmentHidden(tracks, position, seek);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentHidden(ParcelableTrack[] tracks, int position, int seek);
    }

}
