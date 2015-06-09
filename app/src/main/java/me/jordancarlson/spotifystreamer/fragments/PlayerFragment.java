package me.jordancarlson.spotifystreamer.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.jordancarlson.spotifystreamer.R;
import me.jordancarlson.spotifystreamer.adapters.TracksAdapter;
import me.jordancarlson.spotifystreamer.utils.ToolbarUtil;

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
    private static final String TRACK_DUR = "trackDur";
    private static final String ALBUM_IMAGE = "albumImage";
    private static final String TRACK_URL = "trackUrl";

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
    private Handler mHandler = new Handler();


    private String mArtistName;
    private String mAlbumName;
    private String mTrackName;
    private String mTrackUrl;
    private long mTrackDur;
    private String mAlbumImage;

    private OnFragmentInteractionListener mListener;


    public static PlayerFragment newInstance(String artistName, String albumName, String trackName, String trackUrl, long trackDur, String albumImageUrl) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARTIST_NAME, artistName);
        args.putString(ALBUM_NAME, albumName);
        args.putString(TRACK_NAME, trackName);
        args.putString(TRACK_URL, trackUrl);
        args.putLong(TRACK_DUR, trackDur);
        args.putString(ALBUM_IMAGE, albumImageUrl);
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
            mArtistName = getArguments().getString(ARTIST_NAME);
            mAlbumName = getArguments().getString(ALBUM_NAME);
            mTrackName = getArguments().getString(TRACK_NAME);
            mTrackUrl = getArguments().getString(TRACK_URL);
            mTrackDur = getArguments().getLong(TRACK_DUR);
            mAlbumImage = getArguments().getString(ALBUM_IMAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.inject(this, view);

        // check if args are empty

        mArtistTextView.setText(mAlbumName);
        mAlbumTextView.setText(mAlbumName);
        mTrackTextView.setText(mTrackName);

        if (!TextUtils.isEmpty(mAlbumImage)) {
            Picasso.with(getActivity())
                    .load(mAlbumImage)
                    .fit()
                    .centerCrop()
                    .into(mAlbumImageView);
        }

        String trackUrl = mTrackUrl;
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
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
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

        return view;
    }

    Runnable run = new Runnable() {
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        public void onFragmentInteraction(Uri uri);
    }

}
