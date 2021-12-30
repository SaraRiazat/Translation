package com.shariaty.translation.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shariaty.translation.ProjectConstants;
import com.shariaty.translation.R;
import com.shariaty.translation.TranslateApp;
import com.shariaty.translation.local.TranslateDatabase;
import com.shariaty.translation.net.RetrofitBuilder;
import com.shariaty.translation.net.response.TranslateData;
import com.shariaty.translation.net.response.TranslateResponse;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TranslateDetailActivity extends AppCompatActivity {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private TextView textViewQuery;
    private TextView textViewTranslatedText;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ImageView imageViewNoResultsFound;
    private FloatingActionButton buttonFavorite;
    private String translateType;
    private String translateQuery;
    private Long translateId;
    private TranslateDatabase translateDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_detail);
        initialDatabase();
        bindingViews();
        initialViews();
    }

    private void initialDatabase() {
        translateDatabase = ((TranslateApp) getApplicationContext()).getTranslateDatabase();
    }

    private void bindingViews() {
        textViewQuery = findViewById(R.id.text_view_translate_query);
        textViewTranslatedText = findViewById(R.id.text_view_translate_text);
        progressBar = findViewById(R.id.progress_bar_sample_sentences);
        recyclerView = findViewById(R.id.recycler_view_sample_sentences);
        imageViewNoResultsFound = findViewById(R.id.image_view_no_results_found);
        buttonFavorite = findViewById(R.id.floating_action_button_favorite);
    }

    private void initialViews() {
        translateQuery = getIntent().getStringExtra(HomeActivity.TRANSLATE_QUERY_INTENT_NAME);
        translateType = getIntent().getStringExtra(HomeActivity.TRANSLATE_TYPE_INTENT_NAME);
        translateId = Long.parseLong(getIntent().getStringExtra(HomeActivity.TRANSLATE_ID_INTENT_NAME));
        String translateText = getIntent().getStringExtra(HomeActivity.TRANSLATE_TEXT_INTENT_NAME);

        textViewQuery.setText(translateQuery);
        textViewTranslatedText.setText(translateText);
        buttonFavorite.setOnClickListener(view -> {
            buttonFavorite.setImageResource(R.drawable.ic_heart_fill);
            updateFromDatabase();
        });
        getSampleSentences();
    }

    private void updateFromDatabase() {
        Single.fromCallable(() -> {
            translateDatabase.translateDao().updateSentence(translateId, true);
            return 0;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void getSampleSentences() {
        showLoading();

        RetrofitBuilder.getInstance().translate(ProjectConstants.API_TOKEN, translateQuery, null, "like", translateType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<TranslateResponse>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull TranslateResponse translateResponse) {
                        hideLoading();
                        TranslateData translateData = translateResponse.translateData;
                        if (translateData == null) {
                            Toast.makeText(TranslateDetailActivity.this, getString(R.string.token_expired), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (translateData.translateDataResults.isEmpty()) {
                            showNoResultsFound();
                        } else {
                            SampleSentencesAdapter sampleSentencesAdapter = new SampleSentencesAdapter(translateResponse.translateData.translateDataResults);
                            recyclerView.setAdapter(sampleSentencesAdapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(TranslateDetailActivity.this));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        hideLoading();
                        e.printStackTrace();
                    }
                });
    }

    private void showNoResultsFound() {
        imageViewNoResultsFound.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
