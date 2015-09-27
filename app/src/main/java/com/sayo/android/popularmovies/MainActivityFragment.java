package com.sayo.android.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MovieArrayAdapter movieListAdapter;


    public MainActivityFragment() {
    }

    private void updateMovieList() {
        FetchMoviesTask fetchMovieInfo = new FetchMoviesTask();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortingOrder = sharedPref.getString(getString(R.string.pref_movie_sorting_key),
                getString(R.string.pref_movie_sorting_default));

        fetchMovieInfo.execute(sortingOrder);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        updateMovieList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        movieListAdapter =
                new MovieArrayAdapter(
                        getActivity(),
                        new ArrayList<MovieInfo>());

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) view.findViewById(R.id.movies_grid);
        gridView.setAdapter(movieListAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieInfo movieInfo= (MovieInfo) parent.getItemAtPosition(position);
                String movieID = movieInfo.getId();
                Intent otherIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, movieID);

                startActivity(otherIntent);
            }
        });

        return view;
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<MovieInfo>> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();


        private ArrayList<MovieInfo> getMovieDataFromJson(String moviesJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String RESULTS = "results";
            final String POSTER_PATH = "poster_path";
            final String ID = "id";
            final String ORIGINAL_TITLE = "original_title";
            final String OVERVIEW = "overview";
            final String RELEASE_DATE = "release_date";
            final String VOTE_AVERAGE = "vote_average";

            JSONObject root = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = root.getJSONArray(RESULTS);

            ArrayList<MovieInfo> moviesList = new ArrayList<>();

            for (int i = 0; i < moviesArray.length(); i++){
                // Get the JSON object representing the day
                JSONObject movieInfo = moviesArray.getJSONObject(i);

                // http://image.tmdb.org/t/p/w185/kqjL17yufvn9OVLyXYpvtyrFfak.jpg
                String poster_path = Uri.parse("http://image.tmdb.org/t/p/w185").buildUpon()
                                    .appendEncodedPath(movieInfo.getString(POSTER_PATH))
                                    .build().toString();

                String id = movieInfo.getString(ID);
                String original_title = movieInfo.getString(ORIGINAL_TITLE);
                String overview = movieInfo.getString(OVERVIEW);
                String release_date = movieInfo.getString(RELEASE_DATE);
                String vote_average = movieInfo.getString(VOTE_AVERAGE);

                // Only add the movies that have a valid poster to display
                if (movieInfo.getString(POSTER_PATH) != "null") {
                    moviesList.add(new MovieInfo(id, original_title, poster_path, overview,
                            vote_average, release_date));
                }
            }

            return moviesList;
        }

        @Override
        protected void onPostExecute(ArrayList<MovieInfo> results)
        {
            if (results != null)
            {
                movieListAdapter.clear();
                for (MovieInfo movieInfo : results)
                {
                    movieListAdapter.add(movieInfo);
                }
            }
        }

        @Override
        protected ArrayList<MovieInfo> doInBackground(String... params) {

            // If there's no info, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            try{
                // Construct the URL for the OMDB query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=[YOUR API KEY]
                final String MOVIEDB_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, params[0])
                        .appendQueryParameter(API_KEY_PARAM, getString(R.string.api_key))
                        .build();

                Log.v(LOG_TAG, "BuiltURI: "+ builtUri.toString());
                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
                Log.v(LOG_TAG, "BuiltURI Json string: "+ moviesJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(moviesJsonStr);
            }catch (Exception ex) {
                Log.e(LOG_TAG, ex.getMessage(), ex);
                ex.printStackTrace();
            }
            return null;
        }
    }

}
