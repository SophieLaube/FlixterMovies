package com.example.flixtermovies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.flixtermovies.models.Config;
import com.example.flixtermovies.models.Movie;

import cz.msebera.android.httpclient.Header;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // le lien de base pour l'API
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";

    //LE NOM DE PARAMETRE POUR LA CLE DE L'API
    public final static String API_KEY_PARAM = "api_key";
    //tap for logging from this activity
    public final static String TAG = "MainActivity";

    //instance fields
    AsyncHttpClient client;

    // the list of currently playing movies
    ArrayList<Movie> movies;
    // the recycler view
    RecyclerView rvMovies;
    // The adaper wired to the recycler view
    MovieAdapter adapter;
    // image config
    Config config;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new AsyncHttpClient();

        // initialize the list of movies
        movies = new ArrayList<>();
        // initialize the adapter - movies array cannot be reinitialized at this endpoint
        adapter = new MovieAdapter(movies);
        // resolve the recycler view and connect a layout manager and the adapter
        rvMovies = findViewById(R.id.rvMovies);
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        rvMovies.setAdapter(adapter);
        // get hte configuration on app creation
        getConfiguration();
    }

    // get the list of currently playing movies from the api
    private void getNowPlaying(){
        // Create the url
        String url = API_BASE_URL + "/movie/now_playing/";
        // Set the request parameters
        RequestParams params = new RequestParams();
        // API_KEY, ALWAYS REQUIRED
        params.put(API_KEY_PARAM,getString(R.string.api_key));
        // execute a get response expecting a JSONObject response
        client.get(url, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    // load the result into movies list
                    JSONArray results = response.getJSONArray("results");
                    // iterate through result set and Create Movie Object
                    for(int i =0; i< results.length(); i++){
                        Movie movie = new Movie(results.getJSONObject(i));
                        movies.add(movie);
                        // notify to the adapter that a row was added
                        adapter.notifyItemInserted(movies.size() -1);
                    }
                    Log.i(TAG, String.format("Loaded %s movies", results.length()));
                } catch (JSONException e) {
                    logError("Failed to parse now playing movies",e,true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                logError("Failed to get the data from now playing endpoint ",throwable,true);
            }
        });
    }

    private void getConfiguration(){
        // Create the url
        String url = API_BASE_URL + "/configuration";
        // Set the request parameters
        RequestParams params = new RequestParams();
        // API_KEY, ALWAYS REQUIRED
        params.put(API_KEY_PARAM,getString(R.string.api_key));
        // execute a get response expecting a JSONObject response
        client.get(url,params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    config = new Config(response);
                    Log.i(TAG,
                            String.format("Loaded configuration for imageBaseUrl %s and posterSize %s",
                                    config.getImageBaseUrl(), config.getPosterSize()));
                    // pass config to adapter
                    adapter.setConfig(config);
                    // get the now playing movie list
                    getNowPlaying();
                } catch (JSONException e) {
                    logError("Failed parsing configuration",e,true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // invoke logError
                logError("Failed getting configuration",throwable,true);
            }
        });
    }

    // detects silent errors
    private void logError(String message, Throwable error, boolean alertUser){
        // always log the error
        Log.e(TAG,message,error);

        if(alertUser){
            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
        }
    }
}
