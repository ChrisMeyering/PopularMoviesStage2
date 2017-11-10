package com.example.chris.popularMovies2.utilities;

import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chris.popularMovies2.R;
import com.example.chris.popularMovies2.databinding.MovieListItemBinding;
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
    private ArrayList<Integer> favoriteIds = new ArrayList<>();

    public void updateFavorites(ArrayList<Integer> favoriteIds) {
        this.favoriteIds = favoriteIds;
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
        String posterPath = moviePosters.getString(1);
        int movieId = moviePosters.getInt(0);
        if (isFavorite(movieId)) {
            holder.binding.ibFavorite.setImageResource(R.drawable.ic_star_orange_500_24dp);
        } else {
            holder.binding.ibFavorite.setImageResource(R.drawable.ic_star_border_grey_600_24dp);
        }
        holder.binding.pbLoadingPoster.setVisibility(View.VISIBLE);
        Picasso.with(context)
                .load(NetworkUtils.buildPosterURL(posterPath, Utility.getMaxGridCellWidth(context)))
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
    }

    private boolean isFavorite(int movieId) {
        for (int id : favoriteIds) {
            if (movieId == id)
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
            clickHandler.onClick(view, poster);
        }
    }
    public void swapCursor(Cursor data) {
        moviePosters = data;
        notifyDataSetChanged();
    }

}
