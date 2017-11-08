package com.example.chris.popularMovies2.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.chris.popularMovies2.data.MoviesContract;
import com.example.chris.popularMovies2.utilities.JSONUtils;
import com.example.chris.popularMovies2.utilities.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by chris on 11/2/17.
 */

public class MoviesSyncTask {
    private static String TAG = MoviesSyncTask.class.getSimpleName();
    synchronized public static void syncMovies(Context context, String urlString) {
        try {
            URL movieRequestURL = new URL(urlString);
            String jsonQueryResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestURL);
            Log.i(TAG + " -- JSON", jsonQueryResponse);
            ContentValues[] posterValues = JSONUtils.getPosterContentValuesFromJSON(context, jsonQueryResponse);

            if (posterValues != null && posterValues.length != 0) {
                Log.i(TAG, "Saving " + posterValues.length + " new posters");
                ContentResolver contentResolver = context.getContentResolver();
                contentResolver.delete(MoviesContract.CurrentPageEntry.CONTENT_URI, null, null);
                contentResolver.bulkInsert(MoviesContract.CurrentPageEntry.CONTENT_URI, posterValues);
            }
        } catch (MalformedURLException e) {
            Log.i(TAG + " -- URL", "Unable to build url " + urlString);
            e.printStackTrace();

        } catch (IOException e) {
            Log.i(TAG + " -- QUERY", "Unable to query url " + urlString);
            e.printStackTrace();
        } catch (JSONException e ){
            Log.i(TAG + " -- JSON", "Error creating contentValues");
        }
    }
}
