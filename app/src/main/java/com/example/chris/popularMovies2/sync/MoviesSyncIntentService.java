package com.example.chris.popularMovies2.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.chris.popularMovies2.R;

/**
 * Created by chris on 11/2/17.
 */

public class MoviesSyncIntentService extends IntentService {

    private static final String TAG = MoviesSyncIntentService.class.getSimpleName();

    public MoviesSyncIntentService() {
        super("MoviesSyncIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "onHandleIntent");
        if (intent.hasExtra(getResources().getString(R.string.query_url_key)))
            Log.i(TAG, "Starting sync task");
        MoviesSyncTask.syncMovies(this, intent.getStringExtra(getResources().getString(R.string.query_url_key)));
    }
}
