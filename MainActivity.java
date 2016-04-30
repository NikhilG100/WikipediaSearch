package com.example.nikhil.wikipediasearch;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotifySearchResults{

    private EditText urlText;                          //Edit Text field to get input query from user
    private Button searchButton;                       //Search button to get input from user to start query
    private String urlString;                          //URL string
    private final String TAG = "Wikipedia Search";     //Log messages
    private GridView gridView;                         //Grid view
    private ImageAdapter gridAdapter;                  //Image adapter
    private SearchWikipedia searchTask=null;           //Search object
    String [] thumbURLs;                               //Array of thumbanil URL's
    boolean bSearchInProgress=false;                   //Search already in progress?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        urlText = (EditText) findViewById(R.id.searchEditText);
        searchButton = (Button) findViewById(R.id.searchButton);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setBackgroundColor(Color.LTGRAY);
        gridView.setVerticalSpacing(10);
        gridView.setHorizontalSpacing(10);

        gridAdapter = new ImageAdapter();

        //Key listener for ENTER key. As soon as ENTER key is pressed, send HTTP request to Wikipedia.
        urlText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                gridView.setVisibility(View.INVISIBLE);

                // If the event is a key-down event on the "enter" button
               if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    StartSearch(v);
                    return true;
                }
                return false;
            }
        });

        //Search button click listener for Search button. As soon as user clicks, send HTTP request to Wikipedia.
        searchButton.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View view) {
                StartSearch(view);
            }
        });

    }

    //Utility function to send network request
    private void StartSearch(View view) {
        // Gets the URL from the UI's text field.
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
                if (!bSearchInProgress) {
                    gridAdapter.resetArray();
                    urlString = "https://en.wikipedia.org/w/api.php?action=query&prop=pageimages&format=json&piprop=thumbnail&pithumbsize=50&pilimit=50&generator=prefixsearch&gpssearch=" +
                            Uri.encode(urlText.getText().toString());
                    searchTask = new SearchWikipedia();
                    searchTask.setURL(urlString);
                    searchTask.setNotifier(this);
                    bSearchInProgress = true;
                    searchTask.search();
                }
        } else {
            Toast.makeText(view.getContext(), "No Network Connection", Toast.LENGTH_SHORT).show();
        }
    }

    //Final async task callback, to indicate search is complete. Final HTTP response.
    public void OnSearchCompleted() {
        Log.d(TAG, "OnSearchCompleted callback from async task: ");
        gridAdapter.setArray(searchTask.getThumbnailURLS());
        gridView.setVisibility(View.VISIBLE);
        gridView.setAdapter(gridAdapter);
        bSearchInProgress = false;
    }

    //This should be called 4 times from 5 different async tasks, for continued HTTP request and response.
    public void OnSearchInProgress() {
        Log.d(TAG, "OnSearchInProgress callback from async task: ");
        gridAdapter.setArray(searchTask.getThumbnailURLS());
        gridView.setVisibility(View.VISIBLE);
        gridView.setAdapter(gridAdapter);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }
}
