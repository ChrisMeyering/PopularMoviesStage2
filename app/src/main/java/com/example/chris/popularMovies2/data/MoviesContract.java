package com.example.chris.popularMovies2.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by chris on 10/31/17.
 */

public class MoviesContract {
    public static final String CONTENT_AUTHORITY = "com.example.chris.popularMovies2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_RECENT = "recent";
    public static final String PATH_FAVORITES = "favorites";
    public static final String PATH_CURRENT_PAGE = "current";

    public static final class CurrentPageEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_CURRENT_PAGE)
                .build();
        public static final String TABLE_NAME = "current";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static Uri buildCurrentPageUriWithID(int id) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }
    }

    public static final class FavoritesEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITES)
                .build();
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static Uri buildFavoritesUriWithID(int id) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }
    }

    public static final class RecentEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_RECENT)
                .build();
        public static final String TABLE_NAME = "recent";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static Uri buildRecentUriWithID(int id) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }
    }
}
