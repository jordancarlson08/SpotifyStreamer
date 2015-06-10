package me.jordancarlson.spotifystreamer;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import butterknife.ButterKnife;
import me.jordancarlson.spotifystreamer.adapters.ArtistAdapter;
import me.jordancarlson.spotifystreamer.fragments.ArtistSearchFragment;
import me.jordancarlson.spotifystreamer.fragments.PlayerFragment;
import me.jordancarlson.spotifystreamer.fragments.TopTracksFragment;
import me.jordancarlson.spotifystreamer.models.ParcelableArtist;
import me.jordancarlson.spotifystreamer.models.ParcelableTrack;
import me.jordancarlson.spotifystreamer.utils.ToolbarUtil;

/**
 * Main activity, contains the search and list of results for artists.
 */
public class ArtistSearchActivity extends AppCompatActivity implements ArtistSearchFragment.OnFragmentInteractionListener,
        TopTracksFragment.OnFragmentInteractionListener,
        PlayerFragment.OnFragmentInteractionListener {

    private static final String ARTIST_NAME = "artistName";
    private static final String TOP_TRACK_FRAG_TAG = "TOPTRACKFRAG";
    private static final String PLAYER_FRAG_TAG = "PLAYERFRAG";
    private static final String PLAYER_DIALOG_TAG = "PLAYERDIALOG";
    private boolean mTwoPane;
    private String mCurrentArtist;
    private boolean mIsHidden;
    private ParcelableTrack[] mTracks;
    private int mPosition;
    private int mSeek;
//    @InjectView(R.id.artistRecyclerView) RecyclerView mRecyclerView;
//    @InjectView(R.id.searchEditText) EditText mSearchEditText;
//    private ParcelableArtist[] mArtists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);


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
        if (savedInstanceState != null) {
            mCurrentArtist = savedInstanceState.getString(ARTIST_NAME);
        }

        ToolbarUtil.setupToolbar(this, getString(R.string.toolbar_title_search_activity), mCurrentArtist, true);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.now_playing) {
            // do something
            //open up the player

            if(mIsHidden) {
                FragmentManager fragmentManager = getSupportFragmentManager();

                PlayerFragment fragment = PlayerFragment.newInstance(mTracks, mPosition, mSeek);

                if (getResources().getBoolean(R.bool.isTablet)) {

                    fragment.show(fragmentManager, PLAYER_DIALOG_TAG);

                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, fragment, PLAYER_FRAG_TAG)
                            .commit();
                }
            }
//            FragmentManager fragmentManager = getSupportFragmentManager();
//
//            DialogFragment f = (DialogFragment) fragmentManager.findFragmentByTag(PLAYER_DIALOG_TAG);
//            fragmentManager.getFragments();
////            f.show(fragmentManager, PLAYER_DIALOG_TAG);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    public void onNewSearch(String search) {
        mCurrentArtist = null;
        ToolbarUtil.setupToolbar(this, getString(R.string.toolbar_title_search_activity), search, true);
    }

    @Override
    public void onArtistSelected(String artistName) {
        mCurrentArtist = artistName;
        ToolbarUtil.setupToolbar(this, getString(R.string.toolbar_title_search_activity), artistName, true);

    }

    @Override
    public void onFragmentHidden(ParcelableTrack[] tracks, int position, int seek) {
        mIsHidden = true;
        mTracks = tracks;
        mPosition = position;
        mSeek = seek;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARTIST_NAME, mCurrentArtist);

    }



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
