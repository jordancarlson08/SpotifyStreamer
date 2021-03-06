package me.jordancarlson.spotifystreamer.adapters;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import me.jordancarlson.spotifystreamer.R;
import me.jordancarlson.spotifystreamer.fragments.PlayerFragment;
import me.jordancarlson.spotifystreamer.models.ParcelableTrack;

/**
 * Custom adapter for the recycler view of Tracks returned by the Spotify API.
 */
public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.TrackViewHolder> {

    private final ParcelableTrack[] mTracks;

    private Context mContext;

    public TracksAdapter(ParcelableTrack[] tracks) {
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
        holder.bindItem(mTracks, position);
    }

    @Override
    public int getItemCount() {
        return mTracks.length;
    }

    public class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private static final String PLAYER_FRAG_TAG = "PLAYERFRAG";
        private static final String PLAYER_DIALOG_TAG = "PLAYERDIALOG";
        private final TextView mTrackTextView;
        private final TextView mAlbumTextView;
        private final ImageView mAlbumImageView;
        private int mPosition;

        public TrackViewHolder(View itemView) {
            super(itemView);
            mTrackTextView = (TextView) itemView.findViewById(R.id.trackTextView);
            mAlbumTextView = (TextView) itemView.findViewById(R.id.albumTextView);
            mAlbumImageView = (ImageView) itemView.findViewById(R.id.albumImageView);
            itemView.setOnClickListener(this);
        }

        public void bindItem(ParcelableTrack[] tracks, int position) {
            mPosition = position;
            ParcelableTrack track = tracks[position];
            mTrackTextView.setText(track.getTrackName());
            mAlbumTextView.setText(track.getAlbumName());
            Picasso.with(mContext)
                    .load(track.getAlbumImage())
                    .fit()
                    .centerCrop()
                    .into(mAlbumImageView);

        }

        @Override
        public void onClick(View view) {

            FragmentManager fragmentManager = ((AppCompatActivity) mContext).getSupportFragmentManager();
            PlayerFragment fragment = PlayerFragment.newInstance(mTracks, mPosition, false);

            if (mContext.getResources().getBoolean(R.bool.isTablet)) {
                fragment.show(fragmentManager, PLAYER_DIALOG_TAG);
            } else {
                ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, fragment, PLAYER_FRAG_TAG)
                        .commit();
            }

        }
    }
}
