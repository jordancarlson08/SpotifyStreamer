package me.jordancarlson.spotifystreamer.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import java.util.Arrays;

import me.jordancarlson.spotifystreamer.models.ParcelableTrack;
import me.jordancarlson.spotifystreamer.utils.Constants;
import me.jordancarlson.spotifystreamer.utils.MediaPlayerSingleton;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
MediaPlayer.OnErrorListener {

    private MediaPlayer mMediaPlayer;
    private ParcelableTrack[] mTracks;
    private int mPosition;
    private final IBinder mBinder = new LocalBinder();
    private int mSeek;
    private boolean mIsOrientationChange;
    private boolean mIsNowPlaying;

    public MusicService() {
    }

    private void playTrack(ParcelableTrack track) {
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = MediaPlayerSingleton.getInstance().getMediaPlayer();
            }

            // Don't reset the media player if it's an orientation change or coming from the now playing button
            if (!mIsOrientationChange && !mIsNowPlaying) {
                mMediaPlayer.reset();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(track.getTrackUrl());
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.prepareAsync();
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mMediaPlayer.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onCreate() {
        mMediaPlayer = MediaPlayerSingleton.getInstance().getMediaPlayer();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) mMediaPlayer.release();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Parcelable[] parcelables = intent.getParcelableArrayExtra(Constants.TRACKS);
        mTracks = Arrays.copyOf(parcelables, parcelables.length, ParcelableTrack[].class);
        mPosition = intent.getIntExtra(Constants.POSITION, 0);
        mSeek = intent.getIntExtra(Constants.SEEK, 0);
        mIsOrientationChange = intent.getBooleanExtra(Constants.ORIENTATION, false);
        mIsNowPlaying = intent.getBooleanExtra(Constants.NOW_PLAYING, false);
        playTrack(mTracks[mPosition]);

        return super.onStartCommand(intent, flags, startId);
//        return Service.START_STICKY;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!
        return false;
    }


    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public int getCurrentPosition(){
        return mMediaPlayer.getCurrentPosition();
    }
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void pauseMusic(){
        mMediaPlayer.pause();
        Log.d(getClass().getSimpleName(), "pauseMusic()");
    }

    public void playMusic(){
        mMediaPlayer.start();
        Log.d(getClass().getSimpleName(), "playMusic()");
    }
    public void nextSong(){
        mPosition++;
        mIsOrientationChange = false;
        mIsNowPlaying = false;
        if (mPosition < mTracks.length) {
            playTrack(mTracks[mPosition]);

        } else {
            mPosition = 0;
            playTrack(mTracks[mPosition]);
        }
        broadcastSongChanged();
    }
    public void previousSong(){
        mPosition--;
        mIsOrientationChange = false;
        mIsNowPlaying = false;
        if (mPosition >= 0) {
            playTrack(mTracks[mPosition]);

        } else {
            mPosition = 9;
            playTrack(mTracks[mPosition]);
        }
        broadcastSongChanged();
    }
    private void broadcastSongChanged() {
        Intent intent = new Intent(Constants.BROADCAST_RECEIVER);
        intent.putExtra(Constants.TRACK, mTracks[mPosition]);
        sendBroadcast(intent);
    }
    public void stopMusic(){
        mMediaPlayer.stop();
    }
    public void seekTo (int pos){
        mMediaPlayer.seekTo(pos);
    }
    public ParcelableTrack[] getTracks(){
        return mTracks;
    }
    public int getPosition(){
        return mPosition;
    }


    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

}
