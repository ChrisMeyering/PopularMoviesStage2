package com.example.chris.popularMovies2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chris.popularMovies2.data.MoviesContract;
import com.example.chris.popularMovies2.databinding.ActivityMainBinding;
import com.example.chris.popularMovies2.databinding.AppBarMainBinding;
import com.example.chris.popularMovies2.databinding.ContentMainBinding;
import com.example.chris.popularMovies2.sync.MoviesSyncUtils;
import com.example.chris.popularMovies2.utilities.MoviePoster;
import com.example.chris.popularMovies2.utilities.NetworkUtils;
import com.example.chris.popularMovies2.utilities.Utility;

import java.net.URL;
import java.util.ArrayList;

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
    private String sort_by = SORT_BY_RATING;

    private ArrayList<Integer> favorite_ids = new ArrayList<>();
    private int page_number = 1;
    private MovieAdapter movieAdapter;
    private NetworkUtils networkUtils = new NetworkUtils();

    Parcelable layoutManagerSavedState;
    private ActivityMainBinding mainBinding;
    private AppBarMainBinding appBarBinding;
    //private ContentMainBinding contentMainBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        appBarBinding = mainBinding.appBarMainLayout;
        //contentMainBinding = mainBinding.appBarMainLayout.contentMainLayout;
        if (savedInstanceState != null) {
            sort_by = savedInstanceState.getString(getString(R.string.SORT_BY_KEY));
            page_number = savedInstanceState.getInt(getString(R.string.PAGE_NUMBER_KEY));
            layoutManagerSavedState = savedInstanceState.getParcelable(getString(R.string.LAYOUT_MANAGER_KEY));
        }
        initView();
        showError();
        appBarBinding.etSearchByName.setText("HELLO2.5");
        appBarBinding.contentMainLayout.pageGroup.setVisibility(View.VISIBLE);
        if (!sort_by.equals(SORT_FAVORITES)) {
            makeSortedMovieSearch();
        } else {
            findViewById(R.id.page_group).setVisibility(View.GONE);
            queryFavorites();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.getContentResolver().delete(MoviesContract.CurrentPageEntry.CONTENT_URI, null, null);
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
                this, mainBinding.drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                mainBinding.appBarMainLayout.etSearchByName.clearFocus();
                Utility.hideKeyboard(MainActivity.this, getCurrentFocus().getWindowToken());
            }
        };
        mainBinding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mainBinding.navView.setNavigationItemSelectedListener(this);
        switch (sort_by) {
            case SORT_BY_RATING:
                mainBinding.navView.getMenu().getItem(0).setChecked(true);
                break;
            case SORT_BY_POPULARITY:
                mainBinding.navView.getMenu().getItem(1).setChecked(true);
                break;
            case SORT_NOW_PLAYING:
                mainBinding.navView.getMenu().getItem(2).setChecked(true);
                break;
            case SORT_UPCOMING:
                mainBinding.navView.getMenu().getItem(3).setChecked(true);
                break;
            default:
                mainBinding.navView.getMenu().getItem(4).setChecked(true);
                break;

        }
        mainBinding.appBarMainLayout.etSearchByName.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String search = mainBinding.appBarMainLayout.etSearchByName.getText().toString();
                    if (search.trim().length() > 0) {
                        sort_by = "";
                        page_number = 1;
                        updatePageNumberTV();
                        makeMovieQuery(networkUtils.buildSearchURL(getString(R.string.TMDB_search_url_ext), null, search, page_number));
                        findViewById(R.id.page_group).setVisibility(View.VISIBLE);
                    }
                    mainBinding.appBarMainLayout.etSearchByName.clearFocus();
                    Utility.hideKeyboard(MainActivity.this, getCurrentFocus().getWindowToken());
                    return true;
                }
                return false;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initRecyclerView() {
        mainBinding.appBarMainLayout.contentMainLayout.rvMoviePosters.setHasFixedSize(true);
        mainBinding.appBarMainLayout.contentMainLayout.rvMoviePosters.setItemViewCacheSize(30);
        mainBinding.appBarMainLayout.contentMainLayout.rvMoviePosters.setDrawingCacheEnabled(true);
        mainBinding.appBarMainLayout.contentMainLayout.rvMoviePosters.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, Utility.numOfGridColumns(this));
        mainBinding.appBarMainLayout.contentMainLayout.rvMoviePosters.setLayoutManager(layoutManager);
        layoutManager.setAutoMeasureEnabled(true);
        /*
        if (layoutManagerSavedState != null) {
            mainBinding.appBarMainLayout.contentMainLayout.rvMoviePosters.getLayoutManager().onRestoreInstanceState(layoutManagerSavedState);
        }*/
        movieAdapter = new MovieAdapter(this, this);
        mainBinding.appBarMainLayout.contentMainLayout.rvMoviePosters.setAdapter(movieAdapter);
        mainBinding.appBarMainLayout.contentMainLayout.rvMoviePosters.setOnTouchListener(new RecyclerView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mainBinding.appBarMainLayout.etSearchByName.clearFocus();
                Utility.hideKeyboard(MainActivity.this, getCurrentFocus().getWindowToken());
                return false;
            }
        });
    }


    private void updatePageNumberTV() {
        mainBinding.appBarMainLayout.contentMainLayout.tvPageNum.setText(String.valueOf(page_number));
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
        mainBinding.appBarMainLayout.etSearchByName.clearFocus();
        Utility.hideKeyboard(MainActivity.this, getCurrentFocus().getWindowToken());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
        mainBinding.appBarMainLayout.etSearchByName.clearFocus();
        Utility.hideKeyboard(MainActivity.this, getCurrentFocus().getWindowToken());
        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(this, "TODO: Create settings fragment!", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_search:
                String search = mainBinding.appBarMainLayout.etSearchByName.getText().toString();
                if (search.trim().length() == 0) {
                    Toast.makeText(this, "Please enter a movie name.", Toast.LENGTH_SHORT).show();
                    return super.onOptionsItemSelected(item);
                }
                page_number = 1;
                sort_by = "";
                updatePageNumberTV();
                makeMovieQuery(networkUtils.buildSearchURL(getString(R.string.TMDB_search_url_ext), null, search, page_number));
                mainBinding.appBarMainLayout.etSearchByName.clearComposingText();
                findViewById(R.id.page_group).setVisibility(View.VISIBLE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void makeSortedMovieSearch() {
        page_number = 1;
        updatePageNumberTV();
        mainBinding.appBarMainLayout.contentMainLayout.rvMoviePosters.getLayoutManager().scrollToPosition(0);
        makeMovieQuery(networkUtils.buildSearchURL(getString(R.string.TMDB_default_url_ext), sort_by, null, page_number));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mainBinding.drawerLayout.closeDrawer(GravityCompat.START);
        String search = mainBinding.appBarMainLayout.etSearchByName.getText().toString();
        if (search.trim().length() != 0) {
            mainBinding.appBarMainLayout.etSearchByName.setText("");
        }
        int itemId = item.getItemId();
        if (sort_by.equals(SORT_FAVORITES) && itemId != R.id.action_view_favorites)
            findViewById(R.id.page_group).setVisibility(View.VISIBLE);
        else if (!sort_by.equals(SORT_FAVORITES) && itemId == R.id.action_view_favorites)
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
        outState.putParcelable(getString(R.string.LAYOUT_MANAGER_KEY),
                mainBinding.appBarMainLayout.contentMainLayout.rvMoviePosters.getLayoutManager().onSaveInstanceState());
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
                    movieAdapter.updateFavorites(favorite_ids);
                    if (!sort_by.equals(SORT_FAVORITES)) {
                        return;
                    }
                    break;
                case RECENT_MOVIE_LOADER_ID:
                    break;
            }
            Log.i(TAG, "On load finished");
            showMoviePosters();
            movieAdapter.swapCursor(data);
        } else {
            showError();
        }
        mainBinding.appBarMainLayout.contentMainLayout.pbLoadingPosters.setVisibility(View.INVISIBLE);


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


    private void showMoviePosters() {
        mainBinding.appBarMainLayout.contentMainLayout.rvMoviePosters.setVisibility(View.VISIBLE);
        mainBinding.appBarMainLayout.contentMainLayout.tvErrorMessage.setVisibility(View.INVISIBLE);
    }

    private void showError() {
        mainBinding.appBarMainLayout.contentMainLayout.rvMoviePosters.setVisibility(View.INVISIBLE);
        mainBinding.appBarMainLayout.contentMainLayout.tvErrorMessage.setVisibility(View.VISIBLE);
    }


    public void nextPage(View view) {
        page_number++;
        updatePageNumberTV();
        mainBinding.appBarMainLayout.contentMainLayout.rvMoviePosters.getLayoutManager().scrollToPosition(0);
        makeMovieQuery(networkUtils.buildPageURL(page_number));
    }

    public void prevPage(View view) {
        if (page_number > 1) {
            page_number--;
            updatePageNumberTV();
            mainBinding.appBarMainLayout.contentMainLayout.rvMoviePosters.getLayoutManager().scrollToPosition(0);
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
        mainBinding.appBarMainLayout.etSearchByName.clearFocus();
        Utility.hideKeyboard(MainActivity.this, getCurrentFocus().getWindowToken());
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
