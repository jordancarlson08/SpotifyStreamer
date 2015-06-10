package me.jordancarlson.spotifystreamer;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import butterknife.ButterKnife;
import me.jordancarlson.spotifystreamer.adapters.ArtistAdapter;
import me.jordancarlson.spotifystreamer.fragments.ArtistSearchFragment;
import me.jordancarlson.spotifystreamer.fragments.PlayerFragment;
import me.jordancarlson.spotifystreamer.fragments.TopTracksFragment;
import me.jordancarlson.spotifystreamer.models.ParcelableArtist;
import me.jordancarlson.spotifystreamer.utils.ToolbarUtil;

/**
 * Main activity, contains the search and list of results for artists.
 */
public class ArtistSearchActivity extends AppCompatActivity implements ArtistSearchFragment.OnFragmentInteractionListener,
        TopTracksFragment.OnFragmentInteractionListener,
        PlayerFragment.OnFragmentInteractionListener {

    private static final String ARTIST_LIST = "artistList";
    private static final String TOP_TRACK_FRAG_TAG = "TOPTRACKFRAG";
    private boolean mTwoPane;
//    @InjectView(R.id.artistRecyclerView) RecyclerView mRecyclerView;
//    @InjectView(R.id.searchEditText) EditText mSearchEditText;
//    private ParcelableArtist[] mArtists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        ToolbarUtil.setupToolbar(this, getString(R.string.toolbar_title_search_activity), null, true);

        if(findViewById(R.id.fragment_top_tracks_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_top_tracks_container, new TopTracksFragment(), TOP_TRACK_FRAG_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
//
//        if (savedInstanceState != null) {
//            Parcelable[] parcelable = savedInstanceState.getParcelableArray(ARTIST_LIST);
//            ArtistAdapter adapter = null;
//            if (parcelable != null) {
//                mArtists = Arrays.copyOf(parcelable, parcelable.length, ParcelableArtist[].class);
//                adapter = new ArtistAdapter(mArtists);
//            }
//            mRecyclerView.setAdapter(adapter);
//        } else {
//            mRecyclerView.setAdapter(null);
//        }
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ArtistSearchActivity.this);
//        mRecyclerView.setLayoutManager(layoutManager);
//
//
//
//        mSearchEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
//        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
//                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                inputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
//                String searchTerm = mSearchEditText.getText().toString();
//                new FetchArtistsTask().execute(searchTerm);
//
//                return true;
//            }
//        });

    }

    @Override
    public void onArtistSelected(ParcelableArtist[] artists, RecyclerView recyclerView) {
        if(mTwoPane) {
            ArtistAdapter adapter = new ArtistAdapter(artists, true);
            recyclerView.setAdapter(adapter);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
        } else {
            ArtistAdapter adapter = new ArtistAdapter(artists, false);
            recyclerView.setAdapter(adapter);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putParcelableArray(ARTIST_LIST, mArtists);
//
//    }

//    private class FetchArtistsTask extends AsyncTask<String, Void, ParcelableArtist[]> {
//
//        private final ProgressDialog dialog = new ProgressDialog(ArtistSearchActivity.this);
//
//        @Override
//        protected void onPreExecute() {
//            this.dialog.setMessage(getString(R.string.progress_dialog_message));
//            this.dialog.show();
//        }
//
//        @Override
//        protected ParcelableArtist[] doInBackground(String... strings) {
//
//            SpotifyApi api = new SpotifyApi();
//            SpotifyService spotify = api.getService();
//            ArtistsPager results = spotify.searchArtists(strings[0]);
//
//            mArtists = new ParcelableArtist[results.artists.items.size()];
//            for (int i=0; i < results.artists.items.size(); i++) {
//                String imageUrl = "";
//                if (results.artists.items.get(i).images.size() != 0) {
//                    imageUrl = results.artists.items.get(i).images.get(0).url;
//                }
//                ParcelableArtist pa = new ParcelableArtist(results.artists.items.get(i).name, results.artists.items.get(i).id, imageUrl);
//                mArtists[i] = pa;
//            }
//
//            if (results.artists.total == 0) {
//                LinearLayout view = (LinearLayout) findViewById(R.id.mainLayout);
//                Snackbar.make(view, getString(R.string.snackbar_no_artists), Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//
//            return mArtists;
//        }
//
//        @Override
//        protected void onPostExecute(ParcelableArtist[] artists) {
//            super.onPostExecute(artists);
//
//            if (dialog.isShowing()) {
//                dialog.dismiss();
//            }
//
//            ArtistAdapter adapter = new ArtistAdapter(artists);
//            mRecyclerView.setAdapter(adapter);
//            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ArtistSearchActivity.this);
//            mRecyclerView.setLayoutManager(layoutManager);
//
//        }
//    }
}
