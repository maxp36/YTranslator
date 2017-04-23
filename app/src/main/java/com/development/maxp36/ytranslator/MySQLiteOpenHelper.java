package com.development.maxp36.ytranslator;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "HISTORY_ITEMS";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "HISTORY";

    public MySQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "ORIGINAL_TEXT TEXT NOT NULL, "
                    + "TRANSLATED_TEXT TEXT NOT NULL, "
                    + "FROM_LANGUAGE_KEY TEXT NOT NULL, "
                    + "TO_LANGUAGE_KEY TEXT NOT NULL, "
                    + "IS_FAVORITES INTEGER NOT NULL, "
                    + "UNIQUE (ORIGINAL_TEXT, TRANSLATED_TEXT, IS_FAVORITES) ON CONFLICT REPLACE);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
