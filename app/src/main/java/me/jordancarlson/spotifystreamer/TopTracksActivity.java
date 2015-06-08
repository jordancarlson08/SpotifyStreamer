package me.jordancarlson.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.TracksPager;
import me.jordancarlson.spotifystreamer.adapters.ArtistAdapter;
import me.jordancarlson.spotifystreamer.adapters.TracksAdapter;


public class TopTracksActivity extends AppCompatActivity {

    @InjectView(R.id.tracksRecyclerView) RecyclerView mRecyclerView;
    private String mSpotifyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        ButterKnife.inject(this);
        Intent intent = getIntent();

        String artistName = intent.getStringExtra(ArtistAdapter.ARTIST_NAME);
        mSpotifyId = intent.getStringExtra(ArtistAdapter.SPOTIFY_ID);


        new FetchTopTracksTask().execute();


    }

    private class FetchTopTracksTask extends AsyncTask<String, Void, List<Track>> {

        @Override
        protected List<Track> doInBackground(String... Void) {

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            Map<String, Object> params = new HashMap<>();

            // optional todo: add preferences page
            params.put("country", "US");
            Tracks results = spotify.getArtistTopTrack(mSpotifyId, params);

            Log.v("TRACKS>>>>", results.tracks.size()+"");

            List<Track> tracksList = results.tracks;

            if (tracksList.size() == 0) {
                LinearLayout view = (LinearLayout) findViewById(R.id.topTracksLayout);
                Snackbar.make(view, "Sorry, We couldn't find any tracks by that artist.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            return tracksList;
        }

        @Override
        protected void onPostExecute(List<Track> tracksList) {
            super.onPostExecute(tracksList);

            TracksAdapter adapter = new TracksAdapter(tracksList);
            mRecyclerView.setAdapter(adapter);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(TopTracksActivity.this);
            mRecyclerView.setLayoutManager(layoutManager);

        }
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
