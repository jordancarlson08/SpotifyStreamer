package me.jordancarlson.spotifystreamer.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import me.jordancarlson.spotifystreamer.ParcelableArtist;
import me.jordancarlson.spotifystreamer.R;
import me.jordancarlson.spotifystreamer.TopTracksActivity;

/**
 * Custom adapter for the recycler view of Artists returned by the Spotify API.
 */
public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {

    private ParcelableArtist[] mArtists;
    private Context mContext;
    public static final String SPOTIFY_ID = "spotifyId";
    public static final String ARTIST_NAME = "artistName";

    public ArtistAdapter(ParcelableArtist[] artists) {
        mArtists = artists;
    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_artist_search_result, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ArtistViewHolder holder, int position) {
        holder.bindItem(mArtists[position]);

    }

    // TODO: Not sure if this is doing anything helpful. Remove?
    @Override
    public void onViewRecycled(ArtistViewHolder holder) {

        Picasso.with(mContext)
                .cancelRequest(holder.mArtistImageView);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return mArtists.length;
    }

    public class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mArtistTextView;
        private ImageView mArtistImageView;
        private String mSpotifyId;
        private String mArtistName;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            mArtistTextView = (TextView) itemView.findViewById(R.id.artistTextView);
            mArtistImageView = (ImageView) itemView.findViewById(R.id.artistImageView);
            itemView.setOnClickListener(this);
        }

        public void bindItem(ParcelableArtist artist) {
            mArtistName = artist.getArtistName();
            mSpotifyId = artist.getSpotifyId();
            mArtistTextView.setText(artist.getArtistName());
            if (!TextUtils.isEmpty(artist.getImageUrl())) {
                Picasso.with(mContext)
                        .load(artist.getImageUrl())
                        .fit()
                        .centerCrop()
                        .into(mArtistImageView);
            }
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(mContext, TopTracksActivity.class);
            intent.putExtra(ARTIST_NAME, mArtistName);
            intent.putExtra(SPOTIFY_ID, mSpotifyId);
            mContext.startActivity(intent);
        }
    }
}
