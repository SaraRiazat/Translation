package com.shariaty.translation.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TranslateDao {
    @Insert
    long insertSentence(TranslateModel translateModel);

    @Delete
    void removeSentence(TranslateModel translateModel);

    @Query("UPDATE table_translate SET is_favorite = :flag WHERE translate_id = :id")
    void updateSentence(Long id, Boolean flag);

    @Query("SELECT * FROM table_translate")
    List<TranslateModel> getSentences();
}
