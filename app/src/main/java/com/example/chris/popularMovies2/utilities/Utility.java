package com.example.chris.popularMovies2.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;

import com.example.chris.popularMovies2.MainActivity;
import com.example.chris.popularMovies2.data.MoviesContract;

/**
 * Created by chris on 9/28/17.
 */

public class Utility {
    public static int numOfGridColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return Math.max((int) (dpWidth / 160), 3);
    }

    public static int getMaxGridCellWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) (displayMetrics.widthPixels / numOfGridColumns(context) * 0.75);
    }

    public static void hideKeyboard(Context context, IBinder binder) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(binder, 0);
    }


    public static void saveToFavorites(Context context, int id, String posterPath) {
        ContentValues posterValues = new ContentValues();
        posterValues.put(MoviesContract.FavoritesEntry.COLUMN_POSTER_PATH, posterPath);
        posterValues.put(MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID, id);
        context.getContentResolver().insert(MoviesContract.FavoritesEntry.CONTENT_URI, posterValues);
    }

    public static void deleteFromFavorites(Context context, int id) {
        Uri uri = MoviesContract.FavoritesEntry.CONTENT_URI.buildUpon()
                .appendPath(String.valueOf(id))
                .build();
        context.getContentResolver().delete(uri, null, null);
    }
}
