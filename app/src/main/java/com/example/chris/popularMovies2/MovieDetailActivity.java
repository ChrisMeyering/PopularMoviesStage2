package com.example.chris.popularMovies2;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.chris.popularMovies2.databinding.ActivityMovieDetailBinding;
import com.example.chris.popularMovies2.utilities.JSONUtils;
import com.example.chris.popularMovies2.utilities.MovieInfo;
import com.example.chris.popularMovies2.utilities.NetworkUtils;
import com.example.chris.popularMovies2.utilities.ReviewAdapter;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.Arrays;


public class MovieDetailActivity extends YouTubeBaseActivity
        implements LoaderManager.LoaderCallbacks<MovieInfo>,
        YouTubePlayer.OnInitializedListener {

    String TAG = MovieDetailActivity.class.getSimpleName();
    MovieInfo movieInfo = null;
    private int movieID;
    private ReviewAdapter reviewAdapter;
    private ActivityMovieDetailBinding movieDetailBinding;
    private int currentTrailer= 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        movieDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail);

        initRecyclerView();
        Intent startIntent = getIntent();
        if (savedInstanceState != null) {
            movieInfo = savedInstanceState.getParcelable(getString(R.string.MOVIE_INFO_KEY));
            final int[] position = savedInstanceState.getIntArray(getString(R.string.SCROLL_VIEW_POSITION_KEY));
            if (position != null && position.length == 2) {
                movieDetailBinding.svParent.post(new Runnable() {
                    @Override
                    public void run() {
                        movieDetailBinding.svParent.scrollTo(position[0], position[1]);
                    }
                });
            }
            bindMovieInfo();
        } else if (movieInfo != null) {
            bindMovieInfo();
        } else if (startIntent != null) {
            if (startIntent.hasExtra("movieID")) {
                movieID = startIntent.getIntExtra(getString(R.string.MOVIE_ID_KEY), -1);
                makeMovieQuery();
            }
        } else {
            showError();
        }
    }


    private void initRecyclerView() {
        movieDetailBinding.reviewsLayout.rvReviews.setDrawingCacheEnabled(true);
        movieDetailBinding.reviewsLayout.rvReviews.setFocusable(false);
        movieDetailBinding.reviewsLayout.rvReviews.setNestedScrollingEnabled(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        movieDetailBinding.reviewsLayout.rvReviews.setLayoutManager(layoutManager);
        layoutManager.setAutoMeasureEnabled(true);
        reviewAdapter = new ReviewAdapter(this);
        movieDetailBinding.reviewsLayout.rvReviews.setAdapter(reviewAdapter);
    }

    private void makeMovieQuery() {
        URL url = NetworkUtils.buildMovieURL(movieID);
        new MovieQueryTask().execute(url);
    }

    private void showProgressBar() {
        movieDetailBinding.pbLoadingDetails.setVisibility(View.VISIBLE);
        movieDetailBinding.detailsGroup.setVisibility(View.INVISIBLE);
    }

    private void showMovieInfo() {
        movieDetailBinding.detailsGroup.setVisibility(View.VISIBLE);
        movieDetailBinding.tvMovieError.setVisibility(View.INVISIBLE);
        movieDetailBinding.pbLoadingDetails.setVisibility(View.INVISIBLE);
    }

    private void showError() {
        movieDetailBinding.detailsGroup.setVisibility(View.INVISIBLE);
        movieDetailBinding.pbLoadingDetails.setVisibility(View.INVISIBLE);
        movieDetailBinding.tvMovieError.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the VisualizerActivity
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<MovieInfo> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<MovieInfo> loader, MovieInfo data) {

    }

    @Override
    public void onLoaderReset(Loader<MovieInfo> loader) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(getString(R.string.MOVIE_INFO_KEY), movieInfo);
        outState.putIntArray(getString(R.string.SCROLL_VIEW_POSITION_KEY),
                new int[] {movieDetailBinding.svParent.getScrollX(),
                        movieDetailBinding.svParent.getScrollY()});
    }

    private void showImageProgressBars() {
        movieDetailBinding.pbLoadingBackdrop.setVisibility(View.VISIBLE);
        movieDetailBinding.detailsLayout.pbLoadingPoster.setVisibility(View.VISIBLE);
    }

    protected void bindMovieInfo() {
        movieDetailBinding.tvMovieTitle.setText(movieInfo.getTitle());
        movieDetailBinding.detailsLayout.tvReleaseDate.setText(movieInfo.getReleaseDate());
        movieDetailBinding.detailsLayout.tvGenres.setText(movieInfo.getGenreNames());
        movieDetailBinding.detailsLayout.tvRating.setText(String.valueOf(movieInfo.getVoteAverage()) + "/10");
        movieDetailBinding.synopsisLayout.tvMovieInfo.setText(movieInfo.getOverview());
        reviewAdapter.updateData(movieInfo.getReviews());
        if (reviewAdapter.getItemCount() > 0) {
            movieDetailBinding.reviewsGroup.setVisibility(View.VISIBLE);
        }
        String [] trailers = movieInfo.getTrailers();
        if (trailers!= null && trailers.length > 0) {
            movieDetailBinding.youtubePlayer.initialize(BuildConfig.YOUTUBE_API_KEY, this);
            movieDetailBinding.youtubePlayer.setFocusable(true);
        }else {
            movieDetailBinding.youtubePlayer.setVisibility(View.GONE);
            Toast.makeText(this, "No trailers avaliable", Toast.LENGTH_LONG).show();
        }
        showMovieInfo();
        showImageProgressBars();
        Picasso.with(getBaseContext())
                .load(NetworkUtils.buildPosterURL(movieInfo.getPosterPath(), movieDetailBinding.detailsLayout.ivMoviePoster.getWidth()))
                .placeholder(R.drawable.poster_placeholder)
                .error(R.drawable.error)
                .into(movieDetailBinding.detailsLayout.ivMoviePoster, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        movieDetailBinding.detailsLayout.pbLoadingPoster.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError() {
                        movieDetailBinding.detailsLayout.pbLoadingPoster.setVisibility(View.INVISIBLE);
                    }
                });
        Picasso.with(getBaseContext()).
                load(NetworkUtils.buildBackdropURL(movieInfo.getBackdropPath(), movieDetailBinding.ivBackdrop.getWidth()))
                .placeholder(R.drawable.backdrop_placeholder)
                .error(R.drawable.error)
                .into(movieDetailBinding.ivBackdrop, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        movieDetailBinding.pbLoadingBackdrop.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError() {
                        movieDetailBinding.pbLoadingBackdrop.setVisibility(View.INVISIBLE);
                    }
                });

    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        if (!b) {
            String[] trailers = movieInfo.getTrailers();
            Toast.makeText(this, "Loading " + trailers.length + " trailers.", Toast.LENGTH_LONG).show();
            //Videos cued: can navigate through playlist by using left and right arrows in video player
            youTubePlayer.cueVideos(Arrays.asList(trailers), currentTrailer, 0);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        String error = String.format(getString(R.string.youtube_player_error), youTubeInitializationResult.toString());
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        movieDetailBinding.btnTrailer.setVisibility(View.VISIBLE);
    }

    public void watchTrailer(View view) {
        String trailerKey;
        if (movieInfo.getTrailers() != null && movieInfo.getTrailers().length > 0) {

            trailerKey = movieInfo.getTrailers()[currentTrailer];
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailerKey));
            try {
                startActivity(appIntent);
            } catch (ActivityNotFoundException ex) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, NetworkUtils.buildYoutubeUri(trailerKey));
                startActivity(webIntent);
            }
        } else {
            Toast.makeText(this, "Error: Trailer not found.", Toast.LENGTH_LONG).show();
        }
    }

    private class MovieQueryTask extends AsyncTask<URL, Void, MovieInfo> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar();
        }

        @Override
        protected MovieInfo doInBackground(URL... urls) {
            URL url = urls[0];
            String jsonData;
            MovieInfo movie = null;
            try {
                jsonData = NetworkUtils.getResponseFromHttpUrl(url);
                movie = JSONUtils.getMovieDataFromJson(jsonData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return movie;
        }

        @Override
        protected void onPostExecute(final MovieInfo movie) {
            if (movie != null) {
                movieInfo = movie;
                bindMovieInfo();


            } else {
                showError();
            }
        }
    }
}