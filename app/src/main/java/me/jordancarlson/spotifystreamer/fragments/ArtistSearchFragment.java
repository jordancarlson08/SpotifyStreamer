package me.jordancarlson.spotifystreamer.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import me.jordancarlson.spotifystreamer.models.ParcelableArtist;
import me.jordancarlson.spotifystreamer.R;
import me.jordancarlson.spotifystreamer.adapters.ArtistAdapter;

public class ArtistSearchFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String ARTIST_LIST = "artistList";
    @InjectView(R.id.artistRecyclerView) RecyclerView mRecyclerView;
    @InjectView(R.id.searchEditText) EditText mSearchEditText;
    private ParcelableArtist[] mArtists;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ArtistSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ArtistSearchFragment newInstance(String param1, String param2) {
        ArtistSearchFragment fragment = new ArtistSearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ArtistSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_search, container, false);
        ButterKnife.inject(this, view);

        if (savedInstanceState != null) {
            Parcelable[] parcelable = savedInstanceState.getParcelableArray(ARTIST_LIST);
            ArtistAdapter adapter = null;
            if (parcelable != null) {
                mArtists = Arrays.copyOf(parcelable, parcelable.length, ParcelableArtist[].class);
                adapter = new ArtistAdapter(mArtists);
            }
            mRecyclerView.setAdapter(adapter);
        } else {
            mRecyclerView.setAdapter(null);
        }
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        mSearchEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(keyEvent == null || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
                    String searchTerm = mSearchEditText.getText().toString();
                    if (!TextUtils.isEmpty(searchTerm)) {
                        mListener.onNewSearch("Search: " + searchTerm);
                        new FetchArtistsTask().execute(searchTerm);
                    }
                }
                return true;

            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onArtistSelected(mArtists, mRecyclerView);
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
        outState.putParcelableArray(ARTIST_LIST, mArtists);

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
        public void onArtistSelected(ParcelableArtist[] artists, RecyclerView recyclerView);
        public void onNewSearch(String search);
    }

    private class FetchArtistsTask extends AsyncTask<String, Void, ParcelableArtist[]> {

        private final ProgressDialog dialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage(getString(R.string.progress_dialog_message));
            this.dialog.show();
        }

        @Override
        protected ParcelableArtist[] doInBackground(String... strings) {

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager results = spotify.searchArtists(strings[0]);

            mArtists = new ParcelableArtist[results.artists.items.size()];
            if (results.artists.total > 0) {

                for (int i = 0; i < results.artists.items.size(); i++) {
                    String imageUrl = "";
                    if (results.artists.items.get(i).images.size() != 0) {
                        imageUrl = results.artists.items.get(i).images.get(0).url;
                    }
                    ParcelableArtist pa = new ParcelableArtist(results.artists.items.get(i).name, results.artists.items.get(i).id, imageUrl);
                    mArtists[i] = pa;
                }


            } else {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.snackbar_no_artists), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            return mArtists;
        }

        @Override
        protected void onPostExecute(ParcelableArtist[] artists) {
            super.onPostExecute(artists);

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            ArtistAdapter adapter = null;
            if (artists != null) {
                adapter = new ArtistAdapter(artists);
            }
            mRecyclerView.setAdapter(adapter);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(layoutManager);

            mListener.onArtistSelected(artists, mRecyclerView);


        }
    }

}
