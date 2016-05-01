package com.example.nikhil.wikipediasearch;

/**
 * Created by Nikhil on 4/30/2016.
 */

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SearchWikipedia {

    private String searchUrlString = null;             //URL string
    private DownloadWebpageTask webpageTask = null;
    private int nContinuedSearchCounter = 0;           //Counter for async tasks
    private static final int USER_CANCELLED = 1;       //HTTP request user cancelled
    private static final int CONNECTION_SUCCESS = 2;   //HTTP request - connection success
    private static final int SERVER_ERROR = 3;         //HTTP request - Server Error
    private static final int NETWORK_ERROR = 4;        //HTTP request - Network Error
    private JsonReader reader = null;                  //JSON reader
    private String offsetValueForContinuedSearch = ""; //GPS offset value for continued search
    private final String TAG = "Wikipedia Search";     //Log messages
    private String newUrlString = null;                //URL string for continued search
    private static String[] thumbnailURLS = new String[50];
    private int thumbnailCnt = 0;
    private NotifySearchResults notifier;

    public void setURL(String str) {
        searchUrlString = str;
    }

    public void setNotifier(NotifySearchResults n) {
        notifier = n;
    }

    public void search() {
        thumbnailCnt = 0;
        webpageTask = new DownloadWebpageTask();
        webpageTask.execute(searchUrlString);
    }

    public void setImageURL(String imgURL)  {
        thumbnailURLS[thumbnailCnt++] = imgURL;
    }

    public String[] getThumbnailURLS() { return thumbnailURLS; }

    public int getThumbnailCount() { return thumbnailCnt; }

    private class DownloadWebpageTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Dynamically update table image view here
            Log.d(TAG, "OnPostExecute" + result);
            nContinuedSearchCounter++;

            if (!offsetValueForContinuedSearch.isEmpty() && nContinuedSearchCounter < 5) {

                notifier.OnSearchInProgress(); //Notify main UI thread, that search is still in progress.

                newUrlString = searchUrlString + "&gpsoffset=" + offsetValueForContinuedSearch;
                new DownloadWebpageTask().execute(newUrlString);
            }
            else {
                notifier.OnSearchCompleted();
            }

        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null; //Input stream -
            URL url = new URL(myurl);

            Log.d(TAG, "The URL is: " + myurl);
            try {

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                if (isCancelled()) {
                    publishProgress(USER_CANCELLED);
                    return (null);
                }
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(TAG, "The response is: " + response);

                is = conn.getInputStream();

                publishProgress(CONNECTION_SUCCESS);

                getJSONFromUrl(is);

                return myurl;
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... errorCode) {
            switch (errorCode[0]) {
                case USER_CANCELLED:
                    break;
                case NETWORK_ERROR:
                    break;
                case SERVER_ERROR:
                    break;
                case CONNECTION_SUCCESS:
                    break;
            }
        }

        //This method parsers JSON URL from Wikipedia and extracts thumbnail info associated with page.
        public void getJSONFromUrl(InputStream is) throws IOException {
            Boolean bThumbnailFound = false;

            try {
                reader = new JsonReader(new InputStreamReader(is, "UTF-8"));

            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            if (isCancelled()) {
                publishProgress(USER_CANCELLED); //Notify activity that user had canceled the task
                return;
            }

            try {
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("batchcomplete") && reader.peek() != JsonToken.NULL) {
                        String value = reader.nextString();
                    } else if (name.equals("continue")) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String offset = reader.nextName();
                            if (offset.equals("gpsoffset")) {
                                offsetValueForContinuedSearch = reader.nextString();
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    } else if (name.equals("query")) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            reader.skipValue();
                            reader.beginObject();
                            while (reader.hasNext()) {
                                reader.skipValue();
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    String subfield = reader.nextName();
                                    if (subfield.equals("thumbnail")) {
                                        reader.beginObject();
                                        while (reader.hasNext()) {
                                            String thumbnailInfo = reader.nextName();
                                            if (thumbnailInfo.equals("source")) {
                                                setImageURL(reader.nextString());
                                                bThumbnailFound = true;
                                            } else {
                                                reader.skipValue();
                                            }
                                        }
                                        reader.endObject();
                                    } else {
                                        reader.skipValue();
                                    }
                                }
                                if (!bThumbnailFound) {
                                    String noImageURI = "drawable://" + R.drawable.no_image;
                                    setImageURL("http://www.emgreenfield.com/UploadedFiles/Product/no_image.png");
                                } else {
                                    bThumbnailFound = false;
                                }
                                reader.endObject();
                            }
                            reader.endObject();
                        }
                        reader.endObject();
                    }
                    //reader.endObject();
                }
                reader.endObject();

            } catch (IOException e) {
                reader.close();
                e.printStackTrace();
            }

            reader.close();
        }
    }
}
