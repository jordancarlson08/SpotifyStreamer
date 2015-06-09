package me.jordancarlson.spotifystreamer.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;
import me.jordancarlson.spotifystreamer.PlayerActivity;
import me.jordancarlson.spotifystreamer.R;

/**
 * Custom adapter for the recycler view of Tracks returned by the Spotify API.
 */
public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.TrackViewHolder> {

    public static final String ARTIST_NAME = "artistName";
    public static final String ALBUM_NAME = "albumName";
    public static final String ALBUM_IMAGE = "albumImage";
    public static final String TRACK_NAME = "trackName";
    public static final String TRACK_URL = "trackUrl";
    public static final String TRACK_DURATION = "trackDuration";
    private final List<Track> mTracks;
    private Context mContext;
    private String mArtistName;

    public TracksAdapter(List<Track> tracks, String artistName) {
        mTracks = tracks;
        mArtistName = artistName;
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrackViewHolder holder, int position) {
        holder.bindItem(mTracks.get(position));
    }

    @Override
    public int getItemCount() {
        return mTracks.size();
    }

    public class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final TextView mTrackTextView;
        private final TextView mAlbumTextView;
        private final ImageView mAlbumImageView;
        private Track mTrack;

        public TrackViewHolder(View itemView) {
            super(itemView);
            mTrackTextView = (TextView) itemView.findViewById(R.id.trackTextView);
            mAlbumTextView = (TextView) itemView.findViewById(R.id.albumTextView);
            mAlbumImageView = (ImageView) itemView.findViewById(R.id.albumImageView);
            itemView.setOnClickListener(this);
        }

        public void bindItem(Track track) {
            mTrack = track;
            mTrackTextView.setText(track.name);
            mAlbumTextView.setText(track.album.name);
            if (track.album.images.size() != 0) {
                String url = track.album.images.get(0).url;
                Picasso.with(mContext)
                        .load(url)
                        .fit()
                        .centerCrop()
                        .into(mAlbumImageView);
            }
        }

        @Override
        public void onClick(View view) {
            // Use in part 2
            Intent intent = new Intent(mContext, PlayerActivity.class);
            intent.putExtra(ARTIST_NAME, mArtistName);
            intent.putExtra(ALBUM_NAME, mTrack.album.name);
            intent.putExtra(TRACK_NAME, mTrack.name);
            intent.putExtra(TRACK_URL, mTrack.preview_url);
            intent.putExtra(TRACK_DURATION, mTrack.duration_ms);
            if (mTrack.album.images.size() != 0) {
                intent.putExtra(ALBUM_IMAGE, mTrack.album.images.get(0).url);
            }

            mContext.startActivity(intent);

        }
    }
}
