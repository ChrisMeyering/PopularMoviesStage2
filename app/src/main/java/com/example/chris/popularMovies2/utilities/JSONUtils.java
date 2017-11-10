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
        movie.setVoteCount(movieData.getInt(MOV_VOTE_COUNT));
        movie.setId(movieData.getInt(MOV_ID));
        movie.setVideo(movieData.getBoolean(MOV_VIDEO));
        movie.setVoteAverage(movieData.getDouble(MOV_VOTE_AVG));
        movie.setTitle(movieData.getString(MOV_TITLE));
        movie.setPopularity(movieData.getLong(MOV_POPULARITY));
        movie.setPosterPath(movieData.getString(MOV_POSTER));
        movie.setOriginalLanguage(movieData.getString(MOV_ORIG_LANG));
        movie.setOriginalTitle(movieData.getString(MOV_ORIG_TITLE));
        JSONArray JSONGenres = movieData.getJSONArray(MOV_GENRES);
        if (JSONGenres != null) {
            String[] genreNames = new String[JSONGenres.length()];
            for (int j = 0; j < JSONGenres.length(); j++) {
                JSONObject genres = JSONGenres.getJSONObject(j);
                genreNames[j] = genres.getString("name");
            }
            movie.setGenreNames(genreNames);
        }
        movie.setBackdropPath(movieData.getString(MOV_BACKDROP_PATH));
        movie.setAdult(movieData.getBoolean(MOV_ADULT));
        movie.setOverview(movieData.getString(MOV_OVERVIEW));
        movie.setReleaseDate(movieData.getString(MOV_REL_DATE));
        JSONArray JSONVideos = movieData.getJSONObject(MOV_VIDEOS).getJSONArray(RESULTS);
        if (JSONVideos != null) {
            String[] videoKeys = new String[JSONVideos.length()];
            for (int i = 0; i < JSONVideos.length(); i++) {
                JSONObject videoInfo = JSONVideos.getJSONObject(i);
                videoKeys[i] = videoInfo.getString(VID_KEY);
            }
            movie.setTrailers(videoKeys);
        }
        JSONArray JSONReviews = movieData.getJSONObject(MOV_REVIEWS).getJSONArray(RESULTS);
        if (JSONReviews != null && JSONReviews.length() > 0) {
            MovieReview[] movieReviews = new MovieReview[JSONReviews.length()];
            for (int i = 0; i < JSONReviews.length(); i++) {
                JSONObject movieReview = JSONReviews.getJSONObject(i);
                movieReviews[i] = new MovieReview(movieReview.getString(REV_AUTHOR), movieReview.getString(REV_REVIEW));
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
