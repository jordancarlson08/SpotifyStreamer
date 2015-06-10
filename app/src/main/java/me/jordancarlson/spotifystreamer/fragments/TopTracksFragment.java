package me.jordancarlson.spotifystreamer.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import me.jordancarlson.spotifystreamer.R;
import me.jordancarlson.spotifystreamer.adapters.ArtistAdapter;
import me.jordancarlson.spotifystreamer.adapters.TracksAdapter;
import me.jordancarlson.spotifystreamer.models.ParcelableTrack;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TopTracksFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TopTracksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopTracksFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARTIST_NAME = "param1";
    private static final String SPOTIFY_ID = "param2";
    private static final String TRACK_LIST = "trackList";
    @InjectView(R.id.tracksRecyclerView) RecyclerView mRecyclerView;

    // TODO: Rename and change types of parameters
    private String mArtistName;
    private String mSpotifyId;
    private ParcelableTrack[] mTracks;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param artistName Parameter 1.
     * @param spotifyId Parameter 2.
     * @return A new instance of fragment TopTracksFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TopTracksFragment newInstance(String artistName, String spotifyId) {
        TopTracksFragment fragment = new TopTracksFragment();
        Bundle args = new Bundle();
        args.putString(ARTIST_NAME, artistName);
        args.putString(SPOTIFY_ID, spotifyId);
        fragment.setArguments(args);
        return fragment;
    }

    public TopTracksFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mArtistName = getArguments().getString(ARTIST_NAME);
            mSpotifyId = getArguments().getString(SPOTIFY_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_top_tracks, container, false);
        ButterKnife.inject(this, view);

        if (savedInstanceState != null) {
            Parcelable[] parcelable = savedInstanceState.getParcelableArray(TRACK_LIST);
            TracksAdapter adapter = null;
            if (parcelable != null) {
                mTracks = Arrays.copyOf(parcelable, parcelable.length, ParcelableTrack[].class);
                adapter = new TracksAdapter(mTracks);
            }
            mRecyclerView.setAdapter(adapter);
        } else {
            mRecyclerView.setAdapter(null);
        }
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        Intent intent = getActivity().getIntent();
        if (mTracks == null) {
            if (intent != null && intent.getData() != null) {
                String spotifyId = intent.getStringExtra(ArtistAdapter.SPOTIFY_ID);
                mArtistName = intent.getStringExtra(ArtistAdapter.ARTIST_NAME);

                new FetchTopTracksTask().execute(spotifyId);
            } else if (getArguments() != null) {
                Bundle args = getArguments();
                String spotifyId = args.getString(SPOTIFY_ID);
                mArtistName = args.getString(ARTIST_NAME);

                new FetchTopTracksTask().execute(spotifyId);
            }
        }


        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String artistName) {
        if (mListener != null) {
            mListener.onArtistSelected(artistName);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray(TRACK_LIST, mTracks);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onArtistSelected(String artistName);
    }


    private class FetchTopTracksTask extends AsyncTask<String, Void, ParcelableTrack[]> {

        private final ProgressDialog dialog = new ProgressDialog(getActivity());

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
                LinearLayout view = (LinearLayout) getActivity().findViewById(R.id.topTracksLayout);
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

            mListener.onArtistSelected(mArtistName);
            TracksAdapter adapter = new TracksAdapter(tracks);
            mRecyclerView.setAdapter(adapter);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(layoutManager);

        }
    }

}
