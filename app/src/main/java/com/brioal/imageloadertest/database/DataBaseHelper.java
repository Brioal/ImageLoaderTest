package com.brioal.imageloadertest.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Brioal on 2016/4/6.
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    final String CREATE_TABLE = "create table MainHeadItem(_id integer primary key autoincrement ,mImageUrl , mDesc )";


    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
