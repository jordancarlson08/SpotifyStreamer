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

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;
import me.jordancarlson.spotifystreamer.R;
import me.jordancarlson.spotifystreamer.TopTracksActivity;

/**
 * Custom adapter for the recycler view of Tracks returned by the Spotify API.
 */
public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.TrackViewHolder> {

    private List<Track> mTracks;
    private Context mContext;
//    public static final String SPOTIFY_ID = "spotifyId";
//    public static final String ARTIST_NAME = "artistName";

    public TracksAdapter(List<Track> tracks) {
        mTracks = tracks;
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

//    // TODO: Not sure if this is doing anything helpful. Remove?
//    @Override
//    public void onViewRecycled(TrackViewHolder holder) {
//
//        Picasso.with(mContext)
//                .cancelRequest(holder.mArtistImageView);
//        super.onViewRecycled(holder);
//    }

    @Override
    public int getItemCount() {
        return mTracks.size();
    }

    public class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mTrackTextView;
        private TextView mAlbumTextView;
        private ImageView mAlbumImageView;

        public TrackViewHolder(View itemView) {
            super(itemView);
            mTrackTextView = (TextView) itemView.findViewById(R.id.trackTextView);
            mAlbumTextView = (TextView) itemView.findViewById(R.id.albumTextView);
            mAlbumImageView = (ImageView) itemView.findViewById(R.id.albumImageView);
            itemView.setOnClickListener(this);
        }

        public void bindItem(Track track) {
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
//            Intent intent = new Intent(mContext, TopTracksActivity.class);
//            intent.putExtra(ARTIST_NAME, mArtistName);
//            intent.putExtra(SPOTIFY_ID, mSpotifyId);
//            mContext.startActivity(intent);
        }
    }
}