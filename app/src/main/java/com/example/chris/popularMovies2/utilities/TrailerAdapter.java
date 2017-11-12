package com.example.chris.popularMovies2.utilities;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chris.popularMovies2.BuildConfig;
import com.example.chris.popularMovies2.databinding.TrailerListItemBinding;
import com.example.chris.popularMovies2.R;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by chris on 11/11/17.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {
    private static final String TAG = TrailerAdapter.class.getSimpleName();
    private String[] trailerIds;
    private final Context context;
    private final TrailerAdapterClickHandler clickHandler;

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.trailer_list_item, parent, false);
        return new TrailerViewHolder(view);
    }



    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {

        if (trailerIds == null)
            return 0;
        else
            return trailerIds.length;
    }

    public void setData(String[] trailers) {
        trailerIds = trailers;
        notifyDataSetChanged();
    }

    public void releaseLoaders() {

    }

    public interface TrailerAdapterClickHandler {
        void watchTrailer(String trailerKey);
    }

    public TrailerAdapter(Context context, TrailerAdapterClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;
    }


    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TrailerListItemBinding binding;
        public TrailerViewHolder(View view){
            super(view);
            binding = DataBindingUtil.bind(view);
            binding.youtubeThumbView.setOnClickListener(this);
        }

        public void bind() {
            Log.i(TrailerAdapter.class.getSimpleName(), "Binding tailer #" + getAdapterPosition());
            Picasso.with(context)
                    .load(NetworkUtils.buildYoutubeImageURL(trailerIds[getAdapterPosition()]))
                    .placeholder(R.drawable.backdrop_placeholder)
                    .error(R.drawable.error)
                    .into(binding.youtubeThumbView, new Callback() {
                        @Override
                        public void onSuccess() {
                            binding.pbLoadingThumb.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onError() {
                            binding.pbLoadingThumb.setVisibility(View.INVISIBLE);
                        }
                    });
        }

        @Override
        public void onClick(View v) {
            clickHandler.watchTrailer(trailerIds[getAdapterPosition()]);
        }


    }
}
