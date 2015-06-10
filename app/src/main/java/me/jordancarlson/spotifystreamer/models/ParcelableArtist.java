package me.jordancarlson.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable object used for storing the artist name, Spotify Id, and image url.
 */
public class ParcelableArtist implements Parcelable {
    private String mArtistName;
    private String mSpotifyId;
    private String mImageUrl;

    public ParcelableArtist(String artistName, String spotifyId, String imageUrl) {
        mArtistName = artistName;
        mSpotifyId = spotifyId;
        mImageUrl = imageUrl;
    }

    public ParcelableArtist() {
    }

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String artistName) {
        mArtistName = artistName;
    }

    public String getSpotifyId() {
        return mSpotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        mSpotifyId = spotifyId;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mArtistName);
        dest.writeString(this.mSpotifyId);
        dest.writeString(this.mImageUrl);
    }

    private ParcelableArtist(Parcel in) {
        this.mArtistName = in.readString();
        this.mSpotifyId = in.readString();
        this.mImageUrl = in.readString();
    }

    public static final Creator<ParcelableArtist> CREATOR = new Creator<ParcelableArtist>() {
        public ParcelableArtist createFromParcel(Parcel source) {
            return new ParcelableArtist(source);
        }

        public ParcelableArtist[] newArray(int size) {
            return new ParcelableArtist[size];
        }
    };
}
