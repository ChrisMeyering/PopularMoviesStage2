package com.example.chris.popularMovies2.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.chris.popularMovies2.data.MoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chris on 9/26/17.
 */


public class JSONUtils {
    private static final String RESULTS = "results";
    private static final String MOV_VOTE_COUNT = "vote_count";
    private static final String MOV_ID = "id";
    private static final String MOV_VIDEO = "video";
    private static final String MOV_VOTE_AVG = "vote_average";
    private static final String MOV_TITLE = "title";
    private static final String MOV_POPULARITY = "popularity";
    private static final String MOV_POSTER = "poster_path";
    private static final String MOV_ORIG_LANG = "original_language";
    private static final String MOV_ORIG_TITLE = "original_title";
    private static final String MOV_GENRES = "genres";
    private static final String MOV_BACKDROP_PATH = "backdrop_path";
    private static final String MOV_ADULT = "adult";
    private static final String MOV_OVERVIEW = "overview";
    private static final String MOV_REL_DATE = "release_date";
    private static final String MOV_VIDEOS = "videos";
    private static final String MOV_REVIEWS = "reviews";
    private static final String VID_KEY = "key";
    private static final String REV_AUTHOR = "author";
    private static final String REV_REVIEW = "content";

    public static MovieInfo getMovieDataFromJson(String MovieJSONString) throws JSONException {
        MovieInfo movie = new MovieInfo();
        JSONObject movieData = new JSONObject(MovieJSONString);
        movie.setVote_count(movieData.getInt(MOV_VOTE_COUNT));
        movie.setId(movieData.getInt(MOV_ID));
        movie.setVideo(movieData.getBoolean(MOV_VIDEO));
        movie.setVote_average(movieData.getDouble(MOV_VOTE_AVG));
        movie.setTitle(movieData.getString(MOV_TITLE));
        movie.setPopularity(movieData.getLong(MOV_POPULARITY));
        movie.setPoster_path(movieData.getString(MOV_POSTER));
        movie.setOriginal_language(movieData.getString(MOV_ORIG_LANG));
        movie.setOriginal_title(movieData.getString(MOV_ORIG_TITLE));
        JSONArray JSON_Genres = movieData.getJSONArray(MOV_GENRES);
        if (JSON_Genres != null) {
            String[] genre_names = new String[JSON_Genres.length()];
            for (int j = 0; j < JSON_Genres.length(); j++) {
                JSONObject genres = JSON_Genres.getJSONObject(j);
                genre_names[j] = genres.getString("name");
            }
            movie.setGenre_names(genre_names);
        }
        movie.setBackdrop_path(movieData.getString(MOV_BACKDROP_PATH));
        movie.setAdult(movieData.getBoolean(MOV_ADULT));
        movie.setOverview(movieData.getString(MOV_OVERVIEW));
        movie.setRelease_date(movieData.getString(MOV_REL_DATE));
        JSONArray JSON_Videos = movieData.getJSONObject(MOV_VIDEOS).getJSONArray(RESULTS);
        if (JSON_Videos != null) {
            String[] video_keys = new String[JSON_Videos.length()];
            for (int i = 0; i < JSON_Videos.length(); i++) {
                JSONObject video_info = JSON_Videos.getJSONObject(i);
                video_keys[i] = video_info.getString(VID_KEY);
            }
            movie.setTrailers(video_keys);
        }
        JSONArray JSON_Reviews = movieData.getJSONObject(MOV_REVIEWS).getJSONArray(RESULTS);
        if (JSON_Reviews != null && JSON_Reviews.length() > 0) {
            MovieReview[] movieReviews = new MovieReview[JSON_Reviews.length()];
            for (int i = 0; i < JSON_Reviews.length(); i++) {
                JSONObject movie_review = JSON_Reviews.getJSONObject(i);
                Log.i("JSONUITLS", movie_review.toString());
                movieReviews[i] = new MovieReview(movie_review.getString(REV_AUTHOR), movie_review.getString(REV_REVIEW));
            }
            movie.setReviews(movieReviews);
        }
        return movie;
    }


    public static ContentValues[] getPosterContentValuesFromJSON(Context context, String jsonQueryResponse) throws JSONException {
        JSONObject moviesJSON = new JSONObject(jsonQueryResponse);
        JSONArray jsonMoviesArray = moviesJSON.getJSONArray(RESULTS);
        ContentValues[] contentValues = new ContentValues[jsonMoviesArray.length()];
        for (int i = 0; i < jsonMoviesArray.length(); i++) {
            JSONObject movieJSON = jsonMoviesArray.getJSONObject(i);
            ContentValues posterValues = new ContentValues();
            posterValues.put(MoviesContract.CurrentPageEntry.COLUMN_MOVIE_ID, movieJSON.getInt(MOV_ID));
            posterValues.put(MoviesContract.CurrentPageEntry.COLUMN_POSTER_PATH, movieJSON.getString(MOV_POSTER));
            contentValues[i] = posterValues;
        }
        return contentValues;
    }
}
