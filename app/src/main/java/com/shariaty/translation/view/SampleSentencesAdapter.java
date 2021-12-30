package com.shariaty.translation.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shariaty.translation.R;
import com.shariaty.translation.net.response.TranslateDataResult;

import java.util.List;

public class SampleSentencesAdapter extends RecyclerView.Adapter<SampleSentencesAdapter.ViewHolder> {
    private final List<TranslateDataResult> dataSet;

    public SampleSentencesAdapter(List<TranslateDataResult> dataSet) {
        this.dataSet = dataSet;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sample_sentence_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textViewSampleSentence.setText(dataSet.get(position).text);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewSampleSentence;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSampleSentence = itemView.findViewById(R.id.text_view_sample_sentence);
        }
    }
}
