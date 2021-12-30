package com.shariaty.translation.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shariaty.translation.ProjectConstants;
import com.shariaty.translation.R;
import com.shariaty.translation.TranslateApp;
import com.shariaty.translation.local.TranslateDatabase;
import com.shariaty.translation.local.TranslateModel;
import com.shariaty.translation.net.RetrofitBuilder;
import com.shariaty.translation.net.response.TranslateData;
import com.shariaty.translation.net.response.TranslateDataResult;
import com.shariaty.translation.net.response.TranslateResponse;
import com.skydoves.powerspinner.IconSpinnerAdapter;
import com.skydoves.powerspinner.IconSpinnerItem;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity {
    public static final String TRANSLATE_ID_INTENT_NAME = "translate-id";
    public static final String TRANSLATE_QUERY_INTENT_NAME = "translate-query";
    public static final String TRANSLATE_TYPE_INTENT_NAME = "translate-type";
    public static final String TRANSLATE_TEXT_INTENT_NAME = "translate-text";
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private PowerSpinnerView spinnerSourceLanguage;
    private PowerSpinnerView spinnerDestinationLanguage;
    private ImageView imageViewSwap;
    private ImageView imageViewTranslate;
    private EditText editTextSearch;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private TranslateDatabase translateDatabase;
    private RecentlyTranslatedAdapter recentlyTranslatedAdapter;
    private ImageView imageViewRecentlySentenceIsEmpty;
    private TextView textViewRecentlyTranslated;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initialDatabase();
        bindingViews();
        initialViews();
    }

    private void initialDatabase() {
        translateDatabase = ((TranslateApp) getApplicationContext()).getTranslateDatabase();
    }

    private void bindingViews() {
        spinnerSourceLanguage = findViewById(R.id.spinner_source_language);
        spinnerDestinationLanguage = findViewById(R.id.spinner_destination_language);
        imageViewSwap = findViewById(R.id.image_view_swap);
        imageViewTranslate = findViewById(R.id.image_view_search);
        editTextSearch = findViewById(R.id.edit_text_search);
        recyclerView = findViewById(R.id.recycler_view_recently_translated);
        imageViewRecentlySentenceIsEmpty = findViewById(R.id.image_view_no_recently_sentences_found);
        textViewRecentlyTranslated = findViewById(R.id.text_view_recently_translated);
    }

    private void initialSpinnerViews() {
        List<IconSpinnerItem> iconSpinnerItems = new ArrayList<>();
        iconSpinnerItems.add(new IconSpinnerItem(getString(R.string.persian), ContextCompat.getDrawable(this, R.drawable.ic_iran)));
        iconSpinnerItems.add(new IconSpinnerItem(getString(R.string.english), ContextCompat.getDrawable(this, R.drawable.ic_english)));
        iconSpinnerItems.add(new IconSpinnerItem(getString(R.string.arabic), ContextCompat.getDrawable(this, R.drawable.ic_arabic)));
        IconSpinnerAdapter iconSpinnerAdapterSourceLanguage = new IconSpinnerAdapter(spinnerSourceLanguage);
        IconSpinnerAdapter iconSpinnerAdapterDestinationLanguage = new IconSpinnerAdapter(spinnerDestinationLanguage);

        spinnerSourceLanguage.setSpinnerAdapter(iconSpinnerAdapterSourceLanguage);
        spinnerSourceLanguage.setItems(iconSpinnerItems);
        spinnerSourceLanguage.selectItemByIndex(1);
        spinnerSourceLanguage.setLifecycleOwner(this);

        spinnerDestinationLanguage.setSpinnerAdapter(iconSpinnerAdapterDestinationLanguage);
        spinnerDestinationLanguage.setItems(iconSpinnerItems);
        spinnerDestinationLanguage.selectItemByIndex(0);
        spinnerDestinationLanguage.setLifecycleOwner(this);
    }

    private void initialViews() {
        initialSpinnerViews();
        recentlyTranslatedAdapter = new RecentlyTranslatedAdapter(getApplicationContext());

        imageViewSwap.setOnClickListener(view -> swapSpinners());

        imageViewTranslate.setOnClickListener(view -> {
            if (spinnerSourceLanguage.getSelectedIndex() == 0 && spinnerDestinationLanguage.getSelectedIndex() == 1) {
                // fa2en
                showLoading();
                translateFa2En();
            } else if (spinnerSourceLanguage.getSelectedIndex() == 1 && spinnerDestinationLanguage.getSelectedIndex() == 0) {
                // en2fa
                showLoading();
                translateEn2Fa();
            } else if (spinnerSourceLanguage.getSelectedIndex() == 0 && spinnerDestinationLanguage.getSelectedIndex() == 2) {
                // fa2ar
                showLoading();
                translateFa2Ar();
            } else if (spinnerSourceLanguage.getSelectedIndex() == 2 && spinnerDestinationLanguage.getSelectedIndex() == 0) {
                // ar2fa
                showLoading();
                translateAr2Fa();
            } else {
                Toast.makeText(HomeActivity.this, getString(R.string.not_available), Toast.LENGTH_LONG).show();
            }
        });
        getRecentlySentences();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getRecentlySentences();
    }

    private void getRecentlySentences() {
        Single.fromCallable(() -> translateDatabase.translateDao().getSentences())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<TranslateModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull List<TranslateModel> translateModels) {
                        if (translateModels.isEmpty()) {
                            imageViewRecentlySentenceIsEmpty.setVisibility(View.VISIBLE);
                            textViewRecentlyTranslated.setText(R.string.recently_translated_is_empty);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            textViewRecentlyTranslated.setText(R.string.recently_translated);
                            imageViewRecentlySentenceIsEmpty.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            recentlyTranslatedAdapter.setDataSet(translateModels);
                        }
                        recyclerView.setAdapter(recentlyTranslatedAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void showLoading() {
        progressDialog = ProgressDialog.show(HomeActivity.this, "", getString(R.string.loading_please_wait), true);
    }

    private void hideLoading() {
        progressDialog.dismiss();
    }

    private void insertToDatabase(String destinationSentence, String type) {
        TranslateModel translateModel = new TranslateModel();
        translateModel.sourceSentence = editTextSearch.getText().toString();
        translateModel.destinationSentence = destinationSentence;
        translateModel.isFavorite = false;

        Single.fromCallable(() -> translateDatabase.translateDao()
                .insertSentence(translateModel))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Long aLong) {
                        recentlyTranslatedAdapter.addTranslateModel(translateModel);
                        Intent intent = new Intent(HomeActivity.this, TranslateDetailActivity.class);
                        intent.putExtra(TRANSLATE_ID_INTENT_NAME, aLong.toString());
                        intent.putExtra(TRANSLATE_QUERY_INTENT_NAME, translateModel.sourceSentence);
                        intent.putExtra(TRANSLATE_TYPE_INTENT_NAME, type);
                        intent.putExtra(TRANSLATE_TEXT_INTENT_NAME, translateModel.destinationSentence);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void translateAr2Fa() {
        RetrofitBuilder.getInstance().translate(
                ProjectConstants.API_TOKEN,
                editTextSearch.getText().toString(),
                1,
                "exact",
                "ar2fa"
        ).subscribeOn(Schedulers.io())
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
                            Toast.makeText(HomeActivity.this, getString(R.string.token_expired), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        List<TranslateDataResult> translateDataResult = translateData.translateDataResults;
                        if (translateDataResult.isEmpty()) {
                            Toast.makeText(HomeActivity.this, getString(R.string.no_results_found), Toast.LENGTH_SHORT).show();
                        } else {
                            String text = translateDataResult.get(0).text;
                            insertToDatabase(text, "ar2fa");
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        hideLoading();
                        e.printStackTrace();
                    }
                });
    }

    private void translateFa2Ar() {
        RetrofitBuilder.getInstance().translate(
                ProjectConstants.API_TOKEN,
                editTextSearch.getText().toString(),
                1,
                "exact",
                "fa2ar"
        ).subscribeOn(Schedulers.io())
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
                            Toast.makeText(HomeActivity.this, getString(R.string.token_expired), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        List<TranslateDataResult> translateDataResult = translateData.translateDataResults;
                        if (translateDataResult.isEmpty()) {
                            Toast.makeText(HomeActivity.this, getString(R.string.no_results_found), Toast.LENGTH_SHORT).show();
                        } else {
                            String text = translateDataResult.get(0).text;
                            insertToDatabase(text, "fa2ar");
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        hideLoading();
                        e.printStackTrace();
                    }
                });
    }

    private void translateFa2En() {
        RetrofitBuilder.getInstance().translate(
                ProjectConstants.API_TOKEN,
                editTextSearch.getText().toString(),
                1,
                "exact",
                "dehkhoda"
        ).subscribeOn(Schedulers.io())
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
                            Toast.makeText(HomeActivity.this, getString(R.string.token_expired), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        List<TranslateDataResult> translateDataResult = translateData.translateDataResults;
                        if (translateDataResult.isEmpty()) {
                            Toast.makeText(HomeActivity.this, getString(R.string.no_results_found), Toast.LENGTH_SHORT).show();
                        } else {
                            String text = translateDataResult.get(0).text;
                            insertToDatabase(text, "dehkhoda");
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        hideLoading();
                        e.printStackTrace();
                    }
                });
    }

    private void translateEn2Fa() {
        RetrofitBuilder.getInstance().translate(
                ProjectConstants.API_TOKEN,
                editTextSearch.getText().toString(),
                1,
                "exact",
                "en2fa"
        ).subscribeOn(Schedulers.io())
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
                            Toast.makeText(HomeActivity.this, getString(R.string.token_expired), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        List<TranslateDataResult> translateDataResult = translateData.translateDataResults;
                        if (translateDataResult == null || translateDataResult.isEmpty()) {
                            Toast.makeText(HomeActivity.this, getString(R.string.no_results_found), Toast.LENGTH_SHORT).show();
                        } else {
                            String text = translateDataResult.get(0).text;
                            insertToDatabase(text, "en2fa");
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        hideLoading();
                        e.printStackTrace();
                    }
                });
    }

    private void swapSpinners() {
        int lastSpinnerSelectedIndex = spinnerDestinationLanguage.getSelectedIndex();
        spinnerDestinationLanguage.selectItemByIndex(spinnerSourceLanguage.getSelectedIndex());
        spinnerSourceLanguage.selectItemByIndex(lastSpinnerSelectedIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}