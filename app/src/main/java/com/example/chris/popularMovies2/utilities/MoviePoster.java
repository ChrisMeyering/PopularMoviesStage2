package com.example.chris.popularMovies2.utilities;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import com.example.chris.popularMovies2.MainActivity;
import com.example.chris.popularMovies2.data.MoviesContract;

/**
 * Created by chris on 9/27/17.
 */

public class MoviePoster implements Parcelable {
    private final int id;
    private final String poster_path;
    ImageView poster_image;

    public MoviePoster(int id, String poster_path) {
        this.id = id;
        this.poster_path = poster_path;
    }

    protected MoviePoster(Parcel in) {
        id = in.readInt();
        poster_path = in.readString();
    }

    public static final Creator<MoviePoster> CREATOR = new Creator<MoviePoster>() {
        @Override
        public MoviePoster createFromParcel(Parcel in) {
            return new MoviePoster(in);
        }

        @Override
        public MoviePoster[] newArray(int size) {
            return new MoviePoster[size];
        }
    };

    public void setPoster_image (ImageView image) {
        poster_image = image;
    }
    public int getId() {
        return id;
    }

    public void saveToFavorites(Context context) {
        ContentValues posterValues = new ContentValues();
        posterValues.put(MoviesContract.FavoritesEntry.COLUMN_POSTER_PATH, poster_path);
        posterValues.put(MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID, id);
        context.getContentResolver().insert(MoviesContract.FavoritesEntry.CONTENT_URI, posterValues);
    }

    public void deleteFromFavorites(Context context) {
        Uri uri = MoviesContract.FavoritesEntry.CONTENT_URI.buildUpon()
                .appendPath(String.valueOf(id))
                .build();
        context.getContentResolver().delete(uri,null,null );
    }

    public void saveToRecents(Context context) {
        ContentValues posterValues = new ContentValues();
        posterValues.put(MoviesContract.RecentEntry.COLUMN_POSTER_PATH, poster_path);
        posterValues.put(MoviesContract.RecentEntry.COLUMN_MOVIE_ID, id);
        context.getContentResolver().insert(MoviesContract.RecentEntry.CONTENT_URI, posterValues);
    }

    public void saveToCurrent(Context context) {
        ContentValues posterValues = new ContentValues();
        posterValues.put(MoviesContract.CurrentPageEntry.COLUMN_POSTER_PATH, poster_path);
        posterValues.put(MoviesContract.CurrentPageEntry.COLUMN_MOVIE_ID, id);
        context.getContentResolver().insert(MoviesContract.CurrentPageEntry.CONTENT_URI, posterValues);

    }

    public String getPoster_path() {
        return poster_path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(poster_path);
    }

}
