package me.jordancarlson.spotifystreamer.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
    private int mSeek;
    private ServiceConnection mConnection;
    private boolean mBound;
    private MusicService mMusicService;

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
        mPlay.setVisibility(View.GONE);
        mPause.setVisibility(View.VISIBLE);

        startMusicService();





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



        //TODO: Move to service
//        try {
//            mMediaPlayer = MediaPlayerSingleton.getInstance().getMediaPlayer();
//            mMediaPlayer.reset();
//            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mMediaPlayer.setDataSource(trackUrl);
//            mMediaPlayer.prepareAsync();
//            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
//            progressDialog.setMessage(getString(R.string.progress_dialog_message));
//            progressDialog.show();
//            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mediaPlayer) {
//                    progressDialog.dismiss();
//                    if (mSeek > 0) {
//                        mMediaPlayer.seekTo(mSeek);
//                    }
//                    mMediaPlayer.start();
//                    mPlay.setVisibility(View.GONE);
//                    mPause.setVisibility(View.VISIBLE);
//                    mSeekBar.setMax(mMediaPlayer.getDuration());
//                    updateSeekBar();
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void startMusicService() {
        Intent musicService = new Intent(getActivity(), MusicService.class);
        musicService.putExtra(Constants.TRACKS, mTracks);
        musicService.putExtra(Constants.POSITION, mPosition);
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
//            mSeek = mMediaPlayer.getCurrentPosition();
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
        mListener.onFragmentHidden(mTracks, mPosition, mSeek);
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
