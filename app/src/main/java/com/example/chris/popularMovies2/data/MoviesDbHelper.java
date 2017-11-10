package com.example.chris.popularMovies2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.chris.popularMovies2.data.MoviesContract.*;
/**
 * Created by chris on 10/31/17.
 */

public class MoviesDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TAG = MoviesDbHelper.class.getSimpleName();

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAVORITES_TABLE =
                "CREATE TABLE " + FavoritesEntry.TABLE_NAME + " ("
                        + FavoritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + FavoritesEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL UNIQUE, "
                        + FavoritesEntry.COLUMN_POSTER_PATH + " STRING NOT NULL);";
        final String SQL_CREATE_RECENTS_TABLE =
                "CREATE TABLE " + RecentEntry.TABLE_NAME + " ("
                        + RecentEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + RecentEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL UNIQUE, "
                        + RecentEntry.COLUMN_POSTER_PATH + " STRING NOT NULL);";
        final String SQL_CREATE_CURRENT_PAGE_TABLE =
                "CREATE TABLE " + CurrentPageEntry.TABLE_NAME + " ("
                        + CurrentPageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + CurrentPageEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL UNIQUE, "
                        + CurrentPageEntry.COLUMN_POSTER_PATH + " STRING NOT NULL);";

        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
        db.execSQL(SQL_CREATE_RECENTS_TABLE);
        db.execSQL(SQL_CREATE_CURRENT_PAGE_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoritesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RecentEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CurrentPageEntry.TABLE_NAME);
        onCreate(db);
    }
}
