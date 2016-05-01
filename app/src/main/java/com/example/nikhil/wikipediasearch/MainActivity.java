package com.example.nikhil.wikipediasearch;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements NotifySearchResults{

    private EditText urlText;                          //Edit Text field to get input query from user
    private Button searchButton;                       //Search button to get input from user to start query
    private String urlString;                          //URL string
    private final String TAG = "Wikipedia Search";     //Log messages
    private GridView gridView;                         //Grid view
    private ImageAdapter gridAdapter;                  //Image adapter
    private SearchWikipedia searchTask=null;           //Search object
    boolean bSearchInProgress=false;                   //Search already in progress?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        urlText = (EditText) findViewById(R.id.searchEditText);
        urlText.setSelection(urlText.getText().length());

        searchButton = (Button) findViewById(R.id.searchButton);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setBackgroundColor(Color.CYAN);
        gridView.setVerticalSpacing(1);
        gridView.setHorizontalSpacing(1);

        gridAdapter = new ImageAdapter();

        //Key listener for ENTER key. As soon as ENTER key is pressed, send HTTP request to Wikipedia.
        urlText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

               gridView.setVisibility(View.INVISIBLE);

               // If the event is a key-down event on the "enter" button
               if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                   if (!urlText.getText().toString().isEmpty())
                      StartSearch(v);
                    //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                   return true;
               }
               return false;
            }
        });

        //Search button click listener for Search button. As soon as user clicks, send HTTP request to Wikipedia.
        searchButton.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View view) {
                if (!urlText.getText().toString().isEmpty())
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
            Toast toast = Toast.makeText(this, "No Network Connection. Try again later.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
    }

    //Final async task callback, to indicate search is complete. Final HTTP response.
    public void OnSearchCompleted() {
        Log.d(TAG, "OnSearchCompleted callback from async task: ");
        gridAdapter.setArray(searchTask.getThumbnailURLS());

        if (searchTask.getThumbnailCount() == 0) {
            Toast toast = Toast.makeText(this, "No thumbnails found for query.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
        else if (searchTask.getThumbnailCount() > 0) {
            gridView.setVisibility(View.VISIBLE);
            gridView.setAdapter(gridAdapter);
        }

        bSearchInProgress = false;
    }

    //This should be called 4 times from 5 different async tasks, for continued HTTP request and response.
    public void OnSearchInProgress() {
        Log.d(TAG, "OnSearchInProgress callback from async task: ");
        gridAdapter.setArray(searchTask.getThumbnailURLS());
        gridView.setVisibility(View.VISIBLE);
        gridView.setAdapter(gridAdapter);
    }

}
