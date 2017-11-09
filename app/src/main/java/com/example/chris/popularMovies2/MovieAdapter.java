package com.example.chris.popularMovies2;

import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.chris.popularMovies2.databinding.MovieListItemBinding;
import com.example.chris.popularMovies2.utilities.MoviePoster;
import com.example.chris.popularMovies2.utilities.NetworkUtils;
import com.example.chris.popularMovies2.utilities.Utility;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by chris on 9/27/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.PosterViewHolder> {
    private static final String TAG = MovieAdapter.class.getSimpleName();
    private Cursor moviePosters;
    private final Context context;
    private final MovieAdapterClickHandler clickHandler;
    private ArrayList<Integer> favorite_ids = new ArrayList<>();

    public void updateFavorites(ArrayList<Integer> favorite_ids) {
        this.favorite_ids = favorite_ids;
    }


    public interface MovieAdapterClickHandler {
        void onClick(View view, MoviePoster poster);
    }

    public MovieAdapter(Context context, MovieAdapterClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;

    }

    @Override
    public PosterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.movie_list_item, parent, false);
        return new PosterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PosterViewHolder holder, int position) {
        moviePosters.moveToPosition(position);
        String poster_path = moviePosters.getString(1);
        int movie_ID = moviePosters.getInt(0);
        Log.i(TAG, "Movie id = " + movie_ID);
        if (isFavorite(movie_ID)) {
            holder.binding.ibFavorite.setImageResource(R.drawable.ic_star_orange_500_24dp);
        } else {
            holder.binding.ibFavorite.setImageResource(R.drawable.ic_star_border_grey_600_24dp);
        }
        holder.binding.pbLoadingPoster.setVisibility(View.VISIBLE);
        Picasso.with(context)
                .load(NetworkUtils.buildPosterURL(poster_path, Utility.getMaxGridCellWidth(context)))
                .placeholder(R.drawable.poster_placeholder)
                .error(R.drawable.error)
                .into(holder.binding.ivPoster, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.binding.pbLoadingPoster.setVisibility(View.INVISIBLE);
                    }
                    @Override
                    public void onError() {
                        holder.binding.pbLoadingPoster.setVisibility(View.INVISIBLE);
                    }
                });
        //moviePosters.get(position).setPoster_image(holder.iv_poster);
    }

    private boolean isFavorite(int movie_id) {
        for (int id : favorite_ids) {
            if (movie_id == id)
                return true;
        }
        return false;
    }

    @Override
    public int getItemCount() {
        if (moviePosters == null) return 0;
        return moviePosters.getCount();
    }

    public class PosterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private MovieListItemBinding binding;
        public PosterViewHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
            binding.ivPoster.setOnClickListener(this);
            binding.ibFavorite.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {

            moviePosters.moveToPosition(getAdapterPosition());
            if (view.getId() == R.id.ib_favorite) {
                if (isFavorite(moviePosters.getInt(0)))
                    binding.ibFavorite.setImageResource(R.drawable.ic_star_border_grey_600_24dp);
                else
                    binding.ibFavorite.setImageResource(R.drawable.ic_star_orange_500_24dp);
            }
            MoviePoster poster = new MoviePoster(moviePosters.getInt(0),moviePosters.getString(1));
            Log.i(TAG, "Movieid = " + poster.getId());
            clickHandler.onClick(view, poster);
        }
    }
    public void swapCursor(Cursor data) {
        moviePosters = data;
        notifyDataSetChanged();
    }

}
