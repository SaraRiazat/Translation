package com.shariaty.translation.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_translate")
public class TranslateModel {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "translate_id")
    public Long id;
    @ColumnInfo(name = "source_sentence")
    public String sourceSentence;
    @ColumnInfo(name = "destination_sentence")
    public String destinationSentence;
    @ColumnInfo(name = "is_favorite")
    public Boolean isFavorite;
}