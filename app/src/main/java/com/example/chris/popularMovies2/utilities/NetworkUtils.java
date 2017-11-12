package com.example.chris.popularMovies2.utilities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.chris.popularMovies2.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by chris on 10/23/17.
 */

public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();


    private static final String YOUTUBE_API_KEY = BuildConfig.YOUTUBE_API_KEY;
    private static final String YOUTUBE_IMG_BASE_URL = "http://img.youtube.com/vi/";

    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";
    private static final String PARAM_YOUTUBE_QUERY_KEY = "v";

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";
    private static final String TMDB_IMG_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String TMDB_MOVIE_BASE_URL = TMDB_BASE_URL + "movie/";
    private static final String PARAM_API_KEY = "api_key";
    private static final String TMDB_API_KEY = BuildConfig.TMDB_API_KEY;
    private static final String PARAM_PAGE_NUMBER = "page";
    private static final String PARAM_QUERY = "query";
    private static final String PARAM_APPEND_TO_RESPONSE = "append_to_response";
    private static final String APPEND_VIDEOS = "videos";
    private static final String APPEND_REVIEWS = "reviews";


    private static Uri.Builder previousQueryBuilder;

    public static Uri buildYoutubeUri(String video_key) {
        return Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_YOUTUBE_QUERY_KEY, video_key)
                .build();
    }

    public static URL buildMovieURL(int movieID) {
        Uri movieUri = Uri.parse(TMDB_MOVIE_BASE_URL + String.valueOf(movieID)).buildUpon()
                .appendQueryParameter(PARAM_API_KEY, TMDB_API_KEY)
                .appendQueryParameter(PARAM_APPEND_TO_RESPONSE, APPEND_VIDEOS + "," + APPEND_REVIEWS)
                .build();
        URL url = null;
        try {
            url = new URL(movieUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String buildBackdropURL(String path, int width) {
        String imgSize = "w";
        if (width <= 10)
            imgSize += "780";
        else if (width <= 300)
            imgSize += "300";
        else if (width <= 780)
            imgSize += "780";
        else if (width <= 1280)
            imgSize += "1280";
        else
            imgSize = "original";
        return TMDB_IMG_BASE_URL + imgSize + path;
    }

    public static String buildPosterURL(String path, int width) {
        String imgSize = "w";
        if (width <= 10)
            imgSize += "342";
        else if (width <= 92)
            imgSize += "92";
        else if (width <= 154)
            imgSize += "154";
        else if (width <= 185)
            imgSize += "185";
        else if (width <= 342)
            imgSize += "342";
        else if (width <= 500)
            imgSize += "500";
        else if (width <= 780)
            imgSize += "780";
        else
            imgSize = "original";
        return TMDB_IMG_BASE_URL + imgSize + path;
    }

    public static String buildYoutubeImageURL(String trailerId) {
        return YOUTUBE_IMG_BASE_URL + trailerId + "/0.jpg";
    }


    //TODO: use retrofit
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            if (scanner.hasNext())
                return scanner.next();
            else
                return null;
        } finally {
            urlConnection.disconnect();
        }
    }

    public void NetworkUtils() {
        previousQueryBuilder = null;
    }

    public URL buildSearchURL(@NonNull String baseExtension, @Nullable String sortBy, @Nullable String movieName, int pageNumber) {
        Uri.Builder uriBuilder = null;
        String base = TMDB_BASE_URL + baseExtension;
        if ((movieName == null) == (sortBy == null)) {
            throw new IllegalArgumentException("Unable to build requested URL");
        } else if (sortBy != null) {
            uriBuilder = Uri.parse(base + sortBy).buildUpon();
        } else {
            uriBuilder = Uri.parse(base).buildUpon()
                    .appendQueryParameter(PARAM_QUERY, movieName);
        }
        uriBuilder = uriBuilder.appendQueryParameter(PARAM_API_KEY, TMDB_API_KEY);
        previousQueryBuilder = uriBuilder;
        return buildPageURL(pageNumber);
    }

    public URL buildPageURL(int pageNumber) {

        Uri builtUri = previousQueryBuilder.appendQueryParameter(PARAM_PAGE_NUMBER, String.valueOf(pageNumber))
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }


}
