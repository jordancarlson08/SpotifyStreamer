package me.jordancarlson.spotifystreamer.utils;

import android.media.MediaPlayer;

/**
 * Singleton for Mediaplayer Created by jcarlson on 6/10/15.
 */
public class MediaPlayerSingleton {
    private static MediaPlayerSingleton instance = null;
    private final MediaPlayer sMediaPlayer;
    protected MediaPlayerSingleton() {
        sMediaPlayer = new MediaPlayer();
    }
    public static MediaPlayerSingleton getInstance() {
        if(instance == null) {
            instance = new MediaPlayerSingleton();
        }
        return instance;
    }

    public MediaPlayer getsMediaPlayer() {
        return sMediaPlayer;
    }
}