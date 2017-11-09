package com.example.chris.popularMovies2.utilities;

import android.util.Log;

/**
 * Created by chris on 10/23/17.
 */

public class LogUtils {
    public static void logError(String Tag, Exception e) {
        Log.d(Tag, e.getMessage());
        e.printStackTrace();
    }
}
