package com.byted.camp.todolist.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.byted.camp.todolist.db.TodoContract.SQL_CREATE_ENTRIES;
import static com.byted.camp.todolist.db.TodoContract.SQL_DELETE_ENTRIES;

/**
 * Created on 2019/1/22.
 *
 * @author xuyingyi@bytedance.com (Yingyi Xu)
 */
public class TodoDbHelper extends SQLiteOpenHelper {

    // TODO 定义数据库名、版本；创建数据库

    public TodoDbHelper(Context context) {
        super(context, "database.db", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("创建表","1");
        db.execSQL(SQL_CREATE_ENTRIES);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

}
