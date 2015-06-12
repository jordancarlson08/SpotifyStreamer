package me.jordancarlson.spotifystreamer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Arrays;

import butterknife.ButterKnife;
import me.jordancarlson.spotifystreamer.adapters.ArtistAdapter;
import me.jordancarlson.spotifystreamer.fragments.ArtistSearchFragment;
import me.jordancarlson.spotifystreamer.fragments.PlayerFragment;
import me.jordancarlson.spotifystreamer.fragments.TopTracksFragment;
import me.jordancarlson.spotifystreamer.models.ParcelableArtist;
import me.jordancarlson.spotifystreamer.models.ParcelableTrack;
import me.jordancarlson.spotifystreamer.utils.Constants;
import me.jordancarlson.spotifystreamer.utils.ToolbarUtil;

/**
 * Main activity, contains the search and list of results for artists.
 */
public class ArtistSearchActivity extends AppCompatActivity implements ArtistSearchFragment.OnFragmentInteractionListener,
        TopTracksFragment.OnFragmentInteractionListener,
        PlayerFragment.OnFragmentInteractionListener {

    private static final String ARTIST_NAME = "artistName";
    private boolean mTwoPane;
    private String mCurrentArtist;
    private boolean mIsHidden;
    private ParcelableTrack[] mTracks;
    private int mPosition;
    private boolean mIsNowPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);


        if(findViewById(R.id.fragment_top_tracks_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_top_tracks_container, new TopTracksFragment(), Constants.TOP_TRACK_FRAG_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
        if (savedInstanceState != null) {
            mCurrentArtist = savedInstanceState.getString(ARTIST_NAME);
            Parcelable[] parcelables = savedInstanceState.getParcelableArray(Constants.TRACKS);
            mTracks = Arrays.copyOf(parcelables, parcelables.length, ParcelableTrack[].class);
            mPosition = savedInstanceState.getInt(Constants.POSITION);
            mIsHidden = savedInstanceState.getBoolean(Constants.ORIENTATION);
        }

        ToolbarUtil.setupToolbar(this, getString(R.string.toolbar_title_search_activity), mCurrentArtist, true);

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

        if (id == R.id.now_playing) {
            if(mIsHidden) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                //TODO: need to save the state of these for orientation changes
                PlayerFragment fragment = PlayerFragment.newInstance(mTracks, mPosition, true);

                if (getResources().getBoolean(R.bool.isTablet)) {
                    fragment.show(fragmentManager, Constants.PLAYER_DIALOG_TAG);
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, fragment, Constants.PLAYER_FRAG_TAG)
                            .commit();
                }
            }
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
            ArtistAdapter adapter = new ArtistAdapter(artists, false, this);
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
    public void onFragmentHidden(ParcelableTrack[] tracks, int position, boolean isNowPlaying) {
        mIsHidden = true;
        mTracks = tracks;
        mPosition = position;
        mIsNowPlaying = isNowPlaying;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARTIST_NAME, mCurrentArtist);
        outState.putParcelableArray(Constants.TRACKS, mTracks);
        outState.putInt(Constants.POSITION, mPosition);
        outState.putBoolean(Constants.ORIENTATION, mIsHidden);

    }

}
