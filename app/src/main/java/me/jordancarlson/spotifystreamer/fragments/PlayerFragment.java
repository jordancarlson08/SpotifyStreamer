package me.jordancarlson.spotifystreamer.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
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
import me.jordancarlson.spotifystreamer.services.MusicService;
import me.jordancarlson.spotifystreamer.utils.Constants;

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
    private static final String TAG = PlayerFragment.class.getSimpleName();

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
    private final Handler mHandler = new Handler();


    private boolean mHideToolbar;

    private OnFragmentInteractionListener mListener;
    private ParcelableTrack[] mTracks;
    private int mPosition;
    private ServiceConnection mConnection;
    private boolean mBound;
    private MusicService mMusicService;
    private int mSeek;
    private boolean mOrientation;
    private boolean mIsNowPlaying;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                ParcelableTrack track = bundle.getParcelable(Constants.TRACK);
                updateTrackUi(track);
            }
        }
    };


    public static PlayerFragment newInstance(ParcelableTrack[] tracks, int position, boolean isNowPlaying) {
    PlayerFragment fragment = new PlayerFragment();
    Bundle args = new Bundle();
    ParcelableTrack track = tracks[position];
        //TODO: Remove???
    args.putString(ARTIST_NAME, track.getArtistName());
    args.putString(ALBUM_NAME, track.getAlbumName());
    args.putString(TRACK_NAME, track.getTrackName());
    args.putString(TRACK_URL, track.getTrackUrl());
    args.putString(ALBUM_IMAGE, track.getAlbumImage());
    args.putParcelableArray(TRACKS, tracks);
    args.putInt(POSITION, position);
    args.putBoolean(Constants.NOW_PLAYING, isNowPlaying);
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
            Parcelable[] parcelableArray = getArguments().getParcelableArray(TRACKS);
            mTracks = Arrays.copyOf(parcelableArray, parcelableArray.length, ParcelableTrack[].class);
            mPosition = getArguments().getInt(POSITION);
            mIsNowPlaying = getArguments().getBoolean(Constants.NOW_PLAYING);
        }
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {

                MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
                mMusicService = binder.getService();
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.inject(this, view);

        if (savedInstanceState != null) {
            mSeek = savedInstanceState.getInt(Constants.SEEK);
            mOrientation = savedInstanceState.getBoolean(Constants.ORIENTATION);
            Parcelable[] parcelables = savedInstanceState.getParcelableArray(Constants.TRACKS);
            mTracks = Arrays.copyOf(parcelables, parcelables.length, ParcelableTrack[].class);
            mPosition = savedInstanceState.getInt(Constants.POSITION);
        }

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

    private void updateTrackUi(ParcelableTrack track) {
        if (mArtistTextView != null && mAlbumTextView != null && mTrackTextView != null) {
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
        }
    }

    private void playTrack(ParcelableTrack track) {
        updateTrackUi(track);

        if(!mBound) {
            mPlay.setVisibility(View.GONE);
            mPause.setVisibility(View.VISIBLE);
            startMusicService();
        }

        final Handler musicMethodsHandler = new Handler();
        Runnable musicRun = new Runnable() {

            @Override
            public void run() {
                if (mBound) {
                    updateSeekBar();
                }else if(!mBound) {
                    Log.v(TAG, "Waiting to be bound");
                }
                musicMethodsHandler.postDelayed(this, 1000);
            }
        };
        musicMethodsHandler.postDelayed(musicRun, 1000);


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean isFromUser) {
                if (isFromUser) {
                    mMusicService.seekTo(position);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void startMusicService() {

        Intent musicService = new Intent(getActivity(), MusicService.class);
        musicService.putExtra(Constants.TRACKS, mTracks);
        musicService.putExtra(Constants.POSITION, mPosition);
        musicService.putExtra(Constants.SEEK, mSeek);
        musicService.putExtra(Constants.ORIENTATION, mOrientation);
        musicService.putExtra(Constants.NOW_PLAYING, mIsNowPlaying);
        getActivity().startService(musicService);
        getActivity().bindService(musicService, mConnection, Context.BIND_AUTO_CREATE);

    }

    final Runnable run = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
        }
    };

    public void updateSeekBar() {
        if (mMusicService.isPlaying()) {
            mSeekBar.setMax(mMusicService.getDuration());
            mSeekBar.setProgress(mMusicService.getCurrentPosition());
            mHandler.postDelayed(run, 10);
            String elapsed = formatElapsed(mMusicService.getCurrentPosition());
            String remaining = formatRemaining(mMusicService.getDuration(), mMusicService.getCurrentPosition());
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
        if (mBound) {
            mMusicService.playMusic();
            mPlay.setVisibility(View.GONE);
            mPause.setVisibility(View.VISIBLE);
        }


    }
    @OnClick(R.id.pauseButton) public void onPauseClicked() {
        if (mBound) {
            mMusicService.pauseMusic();
            mPlay.setVisibility(View.VISIBLE);
            mPause.setVisibility(View.GONE);
        }

    }
    @OnClick(R.id.previousButton) public void onPreviousClicked() {
        if(mBound) {
            mMusicService.previousSong();
        }
    }
    @OnClick(R.id.nextButton) public void onNextClicked() {
        if(mBound) {
            mMusicService.nextSong();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.SEEK, mMusicService.getCurrentPosition());
        outState.putBoolean(Constants.ORIENTATION, true);
        outState.putParcelableArray(Constants.TRACKS, mMusicService.getTracks());
        outState.putInt(Constants.POSITION, mMusicService.getPosition());
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, new IntentFilter(Constants.BROADCAST_RECEIVER));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
        getActivity().unbindService(mConnection);
        if (mListener != null) {
            mListener.onFragmentHidden(mTracks, mPosition, true);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mHideToolbar = true;
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
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
     * When the fragment is dismissed this callback saves the state of the music for re-creation.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentHidden(ParcelableTrack[] tracks, int position, boolean isNowPlaying);
    }

}
