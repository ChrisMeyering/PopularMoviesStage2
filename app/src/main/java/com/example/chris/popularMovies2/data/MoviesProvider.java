package com.example.chris.popularMovies2.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by chris on 10/31/17.
 */

public class MoviesProvider extends ContentProvider {

    private static final String TAG = MoviesProvider.class.getSimpleName();
    public static final int CODE_FAVORITES = 100;
    public static final int CODE_FAVORITES_WITH_ID = 101;
    public static final int CODE_RECENT = 200;
    public static final int CODE_RECENT_WITH_ID = 201;
    public static final int CODE_CURRENT = 300;
    public static final int CODE_CURRENT_WITH_ID = 301;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;


    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_FAVORITES, CODE_FAVORITES);
        matcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_FAVORITES + "/#", CODE_FAVORITES_WITH_ID);
        matcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_RECENT, CODE_RECENT);
        matcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_RECENT + "/#", CODE_RECENT_WITH_ID);
        matcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_CURRENT_PAGE, CODE_CURRENT);
        matcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_CURRENT_PAGE + "/#", CODE_CURRENT_WITH_ID);
        return matcher;
    }
    @Override
    public boolean onCreate() {
        Log.i(TAG, "onCreate");
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        String[] selectionArguments;
        switch(sUriMatcher.match(uri)) {
            case CODE_FAVORITES:
                cursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.FavoritesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_FAVORITES_WITH_ID:
                selectionArguments = new String[]{uri.getLastPathSegment()};
                cursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.FavoritesEntry.TABLE_NAME,
                        projection,
                        MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_RECENT:
                cursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.RecentEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_RECENT_WITH_ID:
                selectionArguments = new String[]{uri.getLastPathSegment()};
                cursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.RecentEntry.TABLE_NAME,
                        projection,
                        MoviesContract.RecentEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_CURRENT:
                cursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.CurrentPageEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_CURRENT_WITH_ID:
                selectionArguments = new String[]{uri.getLastPathSegment()};
                cursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.CurrentPageEntry.TABLE_NAME,
                        projection,
                        MoviesContract.CurrentPageEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case CODE_CURRENT:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.CurrentPageEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                if(rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInserted;
            default:
                return super.bulkInsert(uri, values);
        }

    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        long _id = -1;
        int movieID = -1;
        try {
            switch (sUriMatcher.match(uri)) {
                case CODE_FAVORITES:
                    movieID = values.getAsInteger(MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID);
                    //String posterPath = values.getAsString(MoviesContract.FavoritesEntry.COLUMN_POSTER_PATH);
                    //long date = values.getAsLong(MoviesContract.FavoritesEntry.COLUMN_DATE);
                    _id = db.insert(MoviesContract.FavoritesEntry.TABLE_NAME,
                            null,
                            values);
                    db.setTransactionSuccessful();
                    break;
                case CODE_RECENT:
                    movieID = values.getAsInteger(MoviesContract.RecentEntry.COLUMN_MOVIE_ID);
                    _id = db.insert(MoviesContract.RecentEntry.TABLE_NAME,
                            null,
                            values);
                    db.setTransactionSuccessful();
                    break;
                case CODE_CURRENT:
                    movieID = values.getAsInteger(MoviesContract.CurrentPageEntry.COLUMN_MOVIE_ID);
                    _id = db.insert(MoviesContract.CurrentPageEntry.TABLE_NAME,
                            null,
                            values);
                    db.setTransactionSuccessful();
                    break;
            }
        } finally {
            db.endTransaction();
        } if (_id != -1 && movieID != -1) {
            getContext().getContentResolver().notifyChange(uri, null);
            return uri.buildUpon().appendPath(String.valueOf(movieID)).build();
        } else {
            return null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int numRowsDeleted;
        String[] selectionArguments;
        if (null == selection) selection = "1";
        switch (sUriMatcher.match(uri)) {
            case CODE_FAVORITES:
                numRowsDeleted = mOpenHelper.getWritableDatabase()
                        .delete(MoviesContract.FavoritesEntry.TABLE_NAME,
                                selection,
                                selectionArgs);
                break;
            case CODE_FAVORITES_WITH_ID:
                selectionArguments = new String[]{uri.getLastPathSegment()};
                Log.i(TAG, "lastPathSegment = " + uri.getLastPathSegment());
                numRowsDeleted = mOpenHelper.getWritableDatabase()
                        .delete(MoviesContract.FavoritesEntry.TABLE_NAME,
                                MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID + " = ?",
                                selectionArguments);
                break;
            case CODE_RECENT:
                numRowsDeleted = mOpenHelper.getWritableDatabase()
                        .delete(MoviesContract.RecentEntry.TABLE_NAME,
                                selection,
                                selectionArgs);
                break;
            case CODE_RECENT_WITH_ID:
                selectionArguments = new String[]{uri.getLastPathSegment()};
                numRowsDeleted = mOpenHelper.getWritableDatabase()
                        .delete(MoviesContract.RecentEntry.TABLE_NAME,
                                MoviesContract.RecentEntry.COLUMN_MOVIE_ID + " = ?",
                                selectionArguments);
                break;
            case CODE_CURRENT:
                numRowsDeleted = mOpenHelper.getWritableDatabase()
                        .delete(MoviesContract.CurrentPageEntry.TABLE_NAME,
                                selection,
                                selectionArgs);
                break;
            case CODE_CURRENT_WITH_ID:
                selectionArguments = new String[]{uri.getLastPathSegment()};
                numRowsDeleted = mOpenHelper.getWritableDatabase()
                        .delete(MoviesContract.CurrentPageEntry.TABLE_NAME,
                                MoviesContract.CurrentPageEntry.COLUMN_MOVIE_ID + " = ?",
                                selectionArguments);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
