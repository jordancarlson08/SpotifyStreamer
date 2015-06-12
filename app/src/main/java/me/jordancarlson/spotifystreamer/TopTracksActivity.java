package me.jordancarlson.spotifystreamer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import me.jordancarlson.spotifystreamer.adapters.ArtistAdapter;
import me.jordancarlson.spotifystreamer.adapters.TracksAdapter;
import me.jordancarlson.spotifystreamer.fragments.PlayerFragment;
import me.jordancarlson.spotifystreamer.models.ParcelableTrack;
import me.jordancarlson.spotifystreamer.utils.ToolbarUtil;


public class TopTracksActivity extends AppCompatActivity implements PlayerFragment.OnFragmentInteractionListener {

    private static final String TRACK_LIST = "trackList";
    @InjectView(R.id.tracksRecyclerView) RecyclerView mRecyclerView;
    private String mArtistName;
    private ParcelableTrack[] mTracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        ButterKnife.inject(this);

        if (savedInstanceState != null) {
            Parcelable[] parcelable = savedInstanceState.getParcelableArray(TRACK_LIST);
            TracksAdapter adapter = null;
            if (parcelable != null) {
                mTracks = Arrays.copyOf(parcelable, parcelable.length, ParcelableTrack[].class);
                mArtistName = mTracks[0].getArtistName();
                adapter = new TracksAdapter(mTracks);
            }
            mRecyclerView.setAdapter(adapter);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(layoutManager);
        }

        Intent intent = getIntent();
        String spotifyId = intent.getStringExtra(ArtistAdapter.SPOTIFY_ID);
        mArtistName = intent.getStringExtra(ArtistAdapter.ARTIST_NAME);
        ToolbarUtil.setupToolbar(this, getString(R.string.toolbar_title_top_tracks_activity), mArtistName, false);

        if (mTracks == null) {
            new FetchTopTracksTask().execute(spotifyId);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray(TRACK_LIST, mTracks);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home){
            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentHidden(ParcelableTrack[] tracks, int position) {

    }

    private class FetchTopTracksTask extends AsyncTask<String, Void, ParcelableTrack[]> {

        private final ProgressDialog dialog = new ProgressDialog(TopTracksActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage(getString(R.string.progress_dialog_message));
            this.dialog.show();
        }

        @Override
        protected ParcelableTrack[] doInBackground(String... strings) {

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            Map<String, Object> params = new HashMap<>();
            params.put("country", "US");

            Tracks results = spotify.getArtistTopTrack(strings[0], params);
            mTracks = new ParcelableTrack[results.tracks.size()];
            for (int i=0; i<results.tracks.size(); i++) {

                Track track = results.tracks.get(i);

                String albumName = track.album.name;
                String albumImage = null;
                if (track.album.images.size() > 0) {
                    albumImage = track.album.images.get(0).url;
                }
                String trackName = track.name;
                String trackUrl = track.preview_url;
                ParcelableTrack pt = new ParcelableTrack(mArtistName, albumName, albumImage, trackName, trackUrl);

                mTracks[i] = pt;
            }

            if (mTracks.length == 0) {
                LinearLayout view = (LinearLayout) findViewById(R.id.topTracksLayout);
                Snackbar.make(view, getString(R.string.snackbar_no_tracks), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            return mTracks;
        }

        @Override
        protected void onPostExecute(ParcelableTrack[] tracks) {
            super.onPostExecute(tracks);

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            TracksAdapter adapter = new TracksAdapter(tracks);
            mRecyclerView.setAdapter(adapter);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(TopTracksActivity.this);
            mRecyclerView.setLayoutManager(layoutManager);

        }
    }
}
