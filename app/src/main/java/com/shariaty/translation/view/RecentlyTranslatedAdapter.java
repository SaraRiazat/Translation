package com.shariaty.translation.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shariaty.translation.R;
import com.shariaty.translation.TranslateApp;
import com.shariaty.translation.local.TranslateModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RecentlyTranslatedAdapter extends RecyclerView.Adapter<RecentlyTranslatedAdapter.ViewHolder> {
    private List<TranslateModel> dataSet = new ArrayList<>();
    private final Context applicationContext;

    public RecentlyTranslatedAdapter(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setDataSet(List<TranslateModel> dataSet) {
        this.dataSet = dataSet;
    }

    public void addTranslateModel(TranslateModel translateModel) {
        if (dataSet.isEmpty()) {
            dataSet.add(translateModel);
            notifyItemInserted(0);
        } else {
            int lastDataSetSize = dataSet.size();
            dataSet.add(translateModel);
            notifyItemInserted(lastDataSetSize);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recently_word_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TranslateModel currentData = dataSet.get(position);

        if (currentData.isFavorite)
            holder.imageViewIsFavorite.setImageResource(R.drawable.ic_heart_fill);
        else holder.imageViewIsFavorite.setImageResource(R.drawable.ic_heart_simple);

        holder.textViewSourceSentence.setText(currentData.sourceSentence);
        holder.textViewDestinationSentence.setText(currentData.destinationSentence);
        holder.imageViewIsFavorite.setOnClickListener(view -> {
            if (currentData.isFavorite) {
                currentData.isFavorite = false;
                holder.imageViewIsFavorite.setImageResource(R.drawable.ic_heart_simple);
            } else {
                currentData.isFavorite = true;
                holder.imageViewIsFavorite.setImageResource(R.drawable.ic_heart_fill);
            }
            updateFromDatabase(currentData.id, currentData.isFavorite);
        });
        holder.imageViewRemove.setOnClickListener(view -> {
            dataSet.remove(currentData);
            notifyItemRemoved(position);
            removeFromDatabase(currentData);
        });
    }

    private void updateFromDatabase(Long id, Boolean isFavorite) {
        Single.fromCallable(() -> {
            ((TranslateApp) applicationContext).getTranslateDatabase()
                    .translateDao()
                    .updateSentence(id, isFavorite);

            return 0;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void removeFromDatabase(TranslateModel translateModel) {
        Single.fromCallable(() -> {
            ((TranslateApp) applicationContext).getTranslateDatabase()
                    .translateDao()
                    .removeSentence(translateModel);

            return 0;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewSourceSentence;
        private final TextView textViewDestinationSentence;
        private final ImageView imageViewIsFavorite;
        private final ImageView imageViewRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSourceSentence = itemView.findViewById(R.id.text_view_source_word);
            textViewDestinationSentence = itemView.findViewById(R.id.text_view_destination_word);
            imageViewIsFavorite = itemView.findViewById(R.id.image_view_heart);
            imageViewRemove = itemView.findViewById(R.id.image_view_close);
        }
    }
}
