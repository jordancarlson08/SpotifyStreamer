package me.jordancarlson.spotifystreamer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import me.jordancarlson.spotifystreamer.adapters.ArtistAdapter;
import me.jordancarlson.spotifystreamer.adapters.TracksAdapter;
import me.jordancarlson.spotifystreamer.utils.ToolbarUtil;


public class TopTracksActivity extends AppCompatActivity {

    @InjectView(R.id.tracksRecyclerView) RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        String spotifyId = intent.getStringExtra(ArtistAdapter.SPOTIFY_ID);
        String artistName = intent.getStringExtra(ArtistAdapter.ARTIST_NAME);

        ToolbarUtil.setupToolbar(this, getString(R.string.toolbar_title_top_tracks_activity), artistName, false);

        new FetchTopTracksTask().execute(spotifyId);
    }

    private class FetchTopTracksTask extends AsyncTask<String, Void, List<Track>> {

        private ProgressDialog dialog = new ProgressDialog(TopTracksActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage(getString(R.string.progress_dialog_message));
            this.dialog.show();
        }

        @Override
        protected List<Track> doInBackground(String... strings) {

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            Map<String, Object> params = new HashMap<>();
            params.put("country", "US");

            Tracks results = spotify.getArtistTopTrack(strings[0], params);

            List<Track> tracksList = results.tracks;

            if (tracksList.size() == 0) {
                LinearLayout view = (LinearLayout) findViewById(R.id.topTracksLayout);
                Snackbar.make(view, getString(R.string.snackbar_no_tracks), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            return tracksList;
        }

        @Override
        protected void onPostExecute(List<Track> tracksList) {
            super.onPostExecute(tracksList);

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            TracksAdapter adapter = new TracksAdapter(tracksList);
            mRecyclerView.setAdapter(adapter);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(TopTracksActivity.this);
            mRecyclerView.setLayoutManager(layoutManager);

        }
    }
}
