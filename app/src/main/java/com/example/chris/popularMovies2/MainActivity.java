package com.example.chris.popularMovies2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chris.popularMovies2.data.MoviesContract;
import com.example.chris.popularMovies2.sync.MoviesSyncUtils;
import com.example.chris.popularMovies2.utilities.MoviePoster;
import com.example.chris.popularMovies2.utilities.NetworkUtils;
import com.example.chris.popularMovies2.utilities.Utility;

import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        NavigationView.OnNavigationItemSelectedListener,
        MovieAdapter.MovieAdapterClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int CURRENT_MOVIE_LOADER_ID = 11;
    private static final int FAVORITES_MOVIE_LOADER_ID = 12;
    private static final int RECENT_MOVIE_LOADER_ID = 13;

    private static final String SORT_BY_RATING = "top_rated";
    private static final String SORT_BY_POPULARITY = "popular";
    private static final String SORT_NOW_PLAYING = "now_playing";
    private static final String SORT_UPCOMING = "upcoming";
    private static final String SORT_FAVORITES = "favorites";
    private static final String SORT_RECENT = "recent";
    @BindView(R.id.rv_movie_posters)
    RecyclerView rv_movies;
    @BindView(R.id.tv_page_num)
    TextView tv_page_num;
    @BindView(R.id.pb_loading_posters)
    ProgressBar pb_loading_data;
    @BindView(R.id.tv_error_message)
    TextView tv_error_message;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.et_search_by_name)
    EditText et_search_by_name;
    private ArrayList<Integer> favorite_ids = new ArrayList<Integer>();
    private int page_number = 1;
    private String sort_by = SORT_BY_RATING;
    private MovieAdapter movieAdapter;
    private NetworkUtils networkUtils = new NetworkUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getContentResolver().delete(MoviesContract.CurrentPageEntry.CONTENT_URI, null, null);

        if (savedInstanceState != null) {
            sort_by = savedInstanceState.getString(getString(R.string.SORT_BY_KEY));
            page_number = savedInstanceState.getInt(getString(R.string.PAGE_NUMBER_KEY));
        }
        initView();
        if (sort_by != SORT_FAVORITES) {
            makeSortedMovieSearch();
        } else {
            findViewById(R.id.page_group).setVisibility(View.GONE);
            queryFavorites();
        }
        //MovieQuery(networkUtils.buildSearchURL(getString(R.string.TMDB_default_url_ext), sort_by, null, page_number));

    }

    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    }

    private void hideKeyboard() {
        et_search_by_name.clearFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private void initView() {
        ButterKnife.bind(this);
        initDrawerLayout();
        initRecyclerView();
        updatePageNumberTV();
    }

    private void initDrawerLayout() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                hideKeyboard();
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        switch (sort_by) {
            case SORT_BY_RATING:
                navigationView.getMenu().getItem(0).setChecked(true);
                break;
            case SORT_BY_POPULARITY:
                navigationView.getMenu().getItem(1).setChecked(true);
                break;
            case SORT_NOW_PLAYING:
                navigationView.getMenu().getItem(2).setChecked(true);
                break;
            case SORT_UPCOMING:
                navigationView.getMenu().getItem(3).setChecked(true);
                break;
            default:
                navigationView.getMenu().getItem(4).setChecked(true);
                break;

        }
        et_search_by_name.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String search = et_search_by_name.getText().toString();
                    if (search.trim().length() > 0) {
                        sort_by = "";
                        page_number = 1;
                        updatePageNumberTV();
                        makeMovieQuery(networkUtils.buildSearchURL(getString(R.string.TMDB_search_url_ext), null, search, page_number));
                        findViewById(R.id.page_group).setVisibility(View.VISIBLE);
                    }
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });
    }

    private void initRecyclerView() {
        rv_movies.setHasFixedSize(true);
        rv_movies.setItemViewCacheSize(30);
        rv_movies.setDrawingCacheEnabled(true);
        rv_movies.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, Utility.numOfGridColumns(this));
        rv_movies.setLayoutManager(layoutManager);
        layoutManager.setAutoMeasureEnabled(true);
        movieAdapter = new MovieAdapter(this, this);
        rv_movies.setAdapter(movieAdapter);
        rv_movies.setOnTouchListener(new RecyclerView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
    }


    private void updatePageNumberTV() {
        tv_page_num.setText(String.valueOf(page_number));
    }

    private void makeMovieQuery(URL url) {
        Log.i(TAG, "Starting movie query");
        MoviesSyncUtils.startImmediateSync(this, url);

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<Cursor> favorites_movie_loader = loaderManager.getLoader(FAVORITES_MOVIE_LOADER_ID);
        if (favorites_movie_loader == null) {
            loaderManager.initLoader(FAVORITES_MOVIE_LOADER_ID, null, this);
        }
        Loader<Cursor> current_movie_loader = loaderManager.getLoader(CURRENT_MOVIE_LOADER_ID);
        if (current_movie_loader == null) {
            loaderManager.initLoader(CURRENT_MOVIE_LOADER_ID, null, this);
        } else {
            loaderManager.restartLoader(CURRENT_MOVIE_LOADER_ID, null, this);
        }
    }

    private void queryFavorites() {
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<Cursor> loader = loaderManager.getLoader(FAVORITES_MOVIE_LOADER_ID);
        if (loader == null) {
            loaderManager.initLoader(FAVORITES_MOVIE_LOADER_ID, null, this);
        } else {
            loaderManager.restartLoader(FAVORITES_MOVIE_LOADER_ID, null, this);
        }
    }


    @Override
    public void onBackPressed() {
        hideKeyboard();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        hideKeyboard();
        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(this, "TODO: Create settings fragment!", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_search:
                String search = et_search_by_name.getText().toString();
                if (search.trim().length() == 0) {
                    Toast.makeText(this, "Please enter a movie name.", Toast.LENGTH_SHORT).show();
                    return super.onOptionsItemSelected(item);
                }
                page_number = 1;
                sort_by = "";
                updatePageNumberTV();
                makeMovieQuery(networkUtils.buildSearchURL(getString(R.string.TMDB_search_url_ext), null, search, page_number));
                et_search_by_name.clearComposingText();
                findViewById(R.id.page_group).setVisibility(View.VISIBLE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void makeSortedMovieSearch() {
        page_number = 1;
        updatePageNumberTV();
        rv_movies.getLayoutManager().scrollToPosition(0);
        makeMovieQuery(networkUtils.buildSearchURL(getString(R.string.TMDB_default_url_ext), sort_by, null, page_number));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        String search = et_search_by_name.getText().toString();
        if (search.trim().length() != 0) {
            et_search_by_name.setText("");
        }
        int itemId = item.getItemId();
        if (sort_by == SORT_FAVORITES && itemId != R.id.action_view_favorites)
            findViewById(R.id.page_group).setVisibility(View.VISIBLE);
        else if (sort_by != SORT_FAVORITES && itemId == R.id.action_view_favorites)
            findViewById(R.id.page_group).setVisibility(View.GONE);
        switch (itemId) {
            case R.id.action_sort_pop:
                if (!sort_by.equals(SORT_BY_POPULARITY)) {
                    sort_by = SORT_BY_POPULARITY;
                    makeSortedMovieSearch();
                }
                break;
            case R.id.action_sort_rating:
                if (!sort_by.equals(SORT_BY_RATING)) {
                    sort_by = SORT_BY_RATING;
                    makeSortedMovieSearch();
                }
                break;
            case R.id.action_now_playing:
                if (!sort_by.equals(SORT_NOW_PLAYING)) {
                    sort_by = SORT_NOW_PLAYING;
                    makeSortedMovieSearch();
                }
                break;
            case R.id.action_upcoming:
                if (!sort_by.equals(SORT_UPCOMING)) {
                    sort_by = SORT_UPCOMING;
                    makeSortedMovieSearch();
                }
                break;
            case R.id.action_view_favorites:
                if (!sort_by.equals(SORT_FAVORITES)) {
                    sort_by = SORT_FAVORITES;
                    queryFavorites();

                }
                break;

        }
        return true;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String sort_state = sort_by;
        outState.putString(getString(R.string.SORT_BY_KEY), sort_state);
        int current_page = page_number;
        outState.putInt(getString(R.string.PAGE_NUMBER_KEY), current_page);

    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        Uri queryUri;
        String sortOrder;
        String[] projection;
        switch (id) {
            case CURRENT_MOVIE_LOADER_ID:
                queryUri = MoviesContract.CurrentPageEntry.CONTENT_URI;
                sortOrder = MoviesContract.CurrentPageEntry._ID + " ASC";
                projection = new String[]{MoviesContract.CurrentPageEntry.COLUMN_MOVIE_ID,
                        MoviesContract.CurrentPageEntry.COLUMN_POSTER_PATH};
                break;
            case FAVORITES_MOVIE_LOADER_ID:

                queryUri = MoviesContract.FavoritesEntry.CONTENT_URI;
                sortOrder = MoviesContract.FavoritesEntry._ID + " ASC";
                projection = new String[]{MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID,
                        MoviesContract.FavoritesEntry.COLUMN_POSTER_PATH};
                break;
            case RECENT_MOVIE_LOADER_ID:
                queryUri = MoviesContract.RecentEntry.CONTENT_URI;
                sortOrder = MoviesContract.RecentEntry._ID + " ASC";
                projection = new String[]{MoviesContract.RecentEntry.COLUMN_MOVIE_ID,
                        MoviesContract.RecentEntry.COLUMN_POSTER_PATH};
                break;
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }


        return new CursorLoader(MainActivity.this,
                queryUri,
                projection,
                null,
                null,
                sortOrder);

/*
        return new AsyncTaskLoader<Cursor>(this) {

                    @Override
                    protected void onStartLoading() {
                        pb_loading_data.setVisibility(View.VISIBLE);
                        forceLoad();
                    }

                    @Override
                    public Cursor loadInBackground() {

                        URL url = null;
                        try {
                            String URLString = args.getString(getString(R.string.query_url_key));
                            url = new URL(URLString);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        String json_data;
                        List<MoviePoster> posters = null;
                        try {
                            json_data = NetworkUtils.getResponseFromHttpUrl(url);
                            Log.i(TAG, json_data);
                            posters = JSONUtils.getPosterDataFromJson(json_data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (posters != null){
                            for (MoviePoster poster : posters) {
                                poster.saveToCurrent(MainActivity.this);
                            }
                        }

                    }

                    @Override
                    public void deliverResult(Cursor data) {
                        super.deliverResult(data);
                        pb_loading_data.setVisibility(View.INVISIBLE);
                    }
                };

*/
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            switch (loader.getId()) {
                case CURRENT_MOVIE_LOADER_ID:
                    break;
                case FAVORITES_MOVIE_LOADER_ID:
                    favorite_ids.clear();
                    for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
                        favorite_ids.add(data.getInt(0));
                    }
                    Log.i(TAG, "updated favorite ids");
                    movieAdapter.updateFavorites(favorite_ids);
                    if (sort_by != SORT_FAVORITES) {
                        return;
                    }
                    break;
                case RECENT_MOVIE_LOADER_ID:
                    break;
            }
            Log.i(TAG, "load finished. Number of items in cursor = " + data.getCount());
            showMoviePosters();
            movieAdapter.swapCursor(data);

            //setMoviePosters(posters);
        } else {
            showError();
        }
        pb_loading_data.setVisibility(View.INVISIBLE);


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


    private void showMoviePosters() {
        rv_movies.setVisibility(View.VISIBLE);
        tv_error_message.setVisibility(View.INVISIBLE);
    }

    private void showError() {
        rv_movies.setVisibility(View.INVISIBLE);
        tv_error_message.setVisibility(View.VISIBLE);
    }


    public void nextPage(View view) {
        page_number++;
        updatePageNumberTV();
        rv_movies.getLayoutManager().scrollToPosition(0);
        makeMovieQuery(networkUtils.buildPageURL(page_number));


    }

    public void prevPage(View view) {
        if (page_number > 1) {
            page_number--;
            updatePageNumberTV();
            rv_movies.getLayoutManager().scrollToPosition(0);
            makeMovieQuery(networkUtils.buildPageURL(page_number));
        }
    }

    private boolean isFavorite(int movie_id) {
        for (int id : favorite_ids) {
            if (movie_id == id)
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View view, MoviePoster movie) {
        hideKeyboard();
        int id = view.getId();
        switch (id) {
            case R.id.iv_poster:
                Intent intent = new Intent(this, MovieDetailActivity.class);
                intent.putExtra("movieID", movie.getId());
                startActivity(intent);
                break;
            case R.id.ib_favorite:
                if (isFavorite(movie.getId())) {
                    movie.deleteFromFavorites(this);
                } else {
                    movie.saveToFavorites(this);
                }
                queryFavorites();
                break;
        }
    }
}
