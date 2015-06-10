package me.jordancarlson.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable object for storing relevant track data
 */
public class ParcelableTrack implements Parcelable {

    private String mArtistName;
    private String mAlbumName;
    private String mAlbumImage;
    private String mTrackName;
    private String mTrackUrl;

    public ParcelableTrack(String artistName, String albumName, String albumImage, String trackName, String trackUrl) {
        mArtistName = artistName;
        mAlbumName = albumName;
        mAlbumImage = albumImage;
        mTrackName = trackName;
        mTrackUrl = trackUrl;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String artistName) {
        mArtistName = artistName;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public void setAlbumName(String albumName) {
        mAlbumName = albumName;
    }

    public String getAlbumImage() {
        return mAlbumImage;
    }

    public void setAlbumImage(String albumImage) {
        mAlbumImage = albumImage;
    }

    public String getTrackName() {
        return mTrackName;
    }

    public void setTrackName(String trackName) {
        mTrackName = trackName;
    }

    public String getTrackUrl() {
        return mTrackUrl;
    }

    public void setTrackUrl(String trackUrl) {
        mTrackUrl = trackUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mArtistName);
        dest.writeString(this.mAlbumName);
        dest.writeString(this.mAlbumImage);
        dest.writeString(this.mTrackName);
        dest.writeString(this.mTrackUrl);
    }

    protected ParcelableTrack(Parcel in) {
        this.mArtistName = in.readString();
        this.mAlbumName = in.readString();
        this.mAlbumImage = in.readString();
        this.mTrackName = in.readString();
        this.mTrackUrl = in.readString();
    }

    public static final Creator<ParcelableTrack> CREATOR = new Creator<ParcelableTrack>() {
        public ParcelableTrack createFromParcel(Parcel source) {
            return new ParcelableTrack(source);
        }

        public ParcelableTrack[] newArray(int size) {
            return new ParcelableTrack[size];
        }
    };
}
