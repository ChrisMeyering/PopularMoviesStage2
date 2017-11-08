package com.example.chris.popularMovies2;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.constraint.Group;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.chris.popularMovies2.utilities.JSONUtils;
import com.example.chris.popularMovies2.utilities.MovieInfo;
import com.example.chris.popularMovies2.utilities.NetworkUtils;
import com.example.chris.popularMovies2.utilities.Utility;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailActivity extends YouTubeBaseActivity
        implements LoaderManager.LoaderCallbacks<MovieInfo>,
        YouTubePlayer.OnInitializedListener{

    private static final int YOUTUBE_RECOVERY_REQUEST = 5;
    String TAG = MovieDetailActivity.class.getSimpleName();

    private int movieID;
    MovieInfo movieInfo = null;

    @BindView(R.id.btn_trailer) Button btn_trailer;
    @BindView(R.id.youtube_player) YouTubePlayerView video_player;
    @BindView(R.id.reviews_layout) View reviews_layout;
    @BindView(R.id.rv_reviews) RecyclerView rv_reviews;
    @BindView(R.id.iv_backdrop) ImageView iv_backdrop;
    @BindView(R.id.tv_movie_title) TextView tv_movie_title;
    @BindView(R.id.tv_release_date) TextView tv_release_date;
    @BindView(R.id.iv_movie_poster) ImageView iv_movie_poster;
    @BindView(R.id.tv_genres) TextView tv_genres;
    @BindView(R.id.tv_rating) TextView tv_rating;
    @BindView(R.id.tv_movie_info) TextView tv_movie_info;

    @BindView(R.id.pb_loading_poster) ProgressBar pb_loading_poster;
    @BindView(R.id.pb_loading_backdrop) ProgressBar pb_loading_backdrop;
    @BindView(R.id.pb_loading_details) ProgressBar pb_loading_details;
    @BindView(R.id.tv_movie_error) TextView tv_display_error;
    @BindView(R.id.details_group) Group details_group;
    ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_movie_detail);

        ButterKnife.bind(this);
        initRecyclerView();
        Intent start_intent = getIntent();
        if (savedInstanceState != null ) {
            movieInfo = savedInstanceState.getParcelable(getString(R.string.MOVIE_INFO_KEY));
            displayMovieInfo();
            Log.i(TAG, "Restoring data from savedinstancestate");
        } else if (movieInfo != null) {
            displayMovieInfo();
        } else if (start_intent != null) {
            if (start_intent.hasExtra("movieID")) {
                movieID = start_intent.getIntExtra("movieID", -1);
                Log.i(TAG, "making movie query");
                makeMovieQuery();
            }
        } else {
            showError();
        }
    }


    private void initRecyclerView() {
        rv_reviews.setDrawingCacheEnabled(true);
        rv_reviews.setNestedScrollingEnabled(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv_reviews.setLayoutManager(layoutManager);
        layoutManager.setAutoMeasureEnabled(true);
        reviewAdapter = new ReviewAdapter(this);
        rv_reviews.setAdapter(reviewAdapter);
    }

    private void makeMovieQuery() {
        URL url = NetworkUtils.buildMovieURL(movieID);
        new MovieQueryTask().execute(url);
    }

    private  void showProgressBar() {
        pb_loading_details.setVisibility(View.VISIBLE);
        details_group.setVisibility(View.INVISIBLE);
    }

    private void showMovieInfo() {
        details_group.setVisibility(View.VISIBLE);
        tv_display_error.setVisibility(View.INVISIBLE);
        pb_loading_details.setVisibility(View.INVISIBLE);
    }

    private void showError() {
        details_group.setVisibility(View.INVISIBLE);
        pb_loading_details.setVisibility(View.INVISIBLE);
        tv_display_error.setVisibility(View.VISIBLE);
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
    }

    private void showImageProgressBars() {
        pb_loading_backdrop.setVisibility(View.VISIBLE);
        pb_loading_poster.setVisibility(View.VISIBLE);
    }

    protected void displayMovieInfo() {

        tv_movie_title.setText(movieInfo.getTitle());
        tv_release_date.append(" " + movieInfo.getRelease_date());
        tv_rating.append(" " + String.valueOf(movieInfo.getVote_average()) + "/10");
        tv_movie_info.setText(movieInfo.getOverview());
        tv_genres.append(" " + movieInfo.getGenre_names());
        reviewAdapter.updateData(movieInfo.getReviews());
        if (reviewAdapter.getItemCount() > 0)
            reviews_layout.setVisibility(View.VISIBLE);
        String[] youtube_key = movieInfo.getTrailers();
        if (youtube_key != null && youtube_key.length > 0) {
            video_player.initialize(BuildConfig.YOUTUBE_API_KEY, this);
        }

        showMovieInfo();
        showImageProgressBars();
        Picasso.with(getBaseContext())
                .load(NetworkUtils.buildPosterURL(movieInfo.getPoster_path(), iv_movie_poster.getWidth()))
                .placeholder(R.drawable.poster_placeholder)
                .error(R.drawable.error)
                .into(iv_movie_poster, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        pb_loading_poster.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError() {
                        pb_loading_poster.setVisibility(View.INVISIBLE);
                    }
                });
        Picasso.with(getBaseContext()).
                load(NetworkUtils.buildBackdropURL(movieInfo.getBackdrop_path(), iv_backdrop.getWidth()))
                .placeholder(R.drawable.backdrop_placeholder)
                .error(R.drawable.error)
                .into(iv_backdrop, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        pb_loading_backdrop.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError() {
                        pb_loading_backdrop.setVisibility(View.INVISIBLE);
                    }
                });
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        if (!b) {
            youTubePlayer.cueVideo(movieInfo.getTrailers()[0]);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(this, YOUTUBE_RECOVERY_REQUEST).show();
        } else {
            String error = String.format(getString(R.string.youtube_player_error), youTubeInitializationResult.toString());
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            video_player.setVisibility(View.INVISIBLE);
            btn_trailer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == YOUTUBE_RECOVERY_REQUEST) {
            video_player.initialize(BuildConfig.YOUTUBE_API_KEY, this);
        }
    }

    public void watchTrailer(View view) {
        String trailerKey = "";
        if (movieInfo.getTrailers() != null && movieInfo.getTrailers().length > 0) {
            trailerKey = movieInfo.getTrailers()[0];
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
            String json_data;
            MovieInfo movie = null;
            try {
                json_data = NetworkUtils.getResponseFromHttpUrl(url);
                Log.i(TAG, "json response = " + json_data);
                movie = JSONUtils.getMovieDataFromJson(json_data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return movie;
        }

        @Override
        protected void onPostExecute(final MovieInfo movie) {
            final Context context = getBaseContext();
            if (movie != null) {
                movieInfo = movie;
                displayMovieInfo();


            } else {
                showError();
            }
        }
    }
}
