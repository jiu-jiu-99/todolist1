package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.activity.DatabaseActivity;
import com.byted.camp.todolist.operation.activity.DebugActivity;
import com.byted.camp.todolist.operation.activity.SettingActivity;
import com.byted.camp.todolist.operation.db.FeedReaderContract;
import com.byted.camp.todolist.operation.db.FeedReaderDbHelper;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.byted.camp.todolist.db.TodoContract.FeedEntry;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;

    private TodoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"tiaozhuan");
                startActivityForResult(

                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        dbHelper = new TodoDbHelper(this);
        //SQLiteDatabase db = dbHelper.getWritableDatabase();
        //dbHelper.onUpgrade(db,1,2);
        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        Log.i(TAG, "perfrom query data:");
        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            case R.id.action_database:
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans

        Log.i(TAG, "perfrom query data2:");
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        List<Note> noteList = new ArrayList<>();

        String[] projection = {
                BaseColumns._ID,
                FeedEntry.COLUMN_NAME_DATE,
                FeedEntry.COLUMN_NAME_STATE,
                FeedEntry.COLUMN_NAME_CONTENT
        };

        String sortOrder =
                FeedEntry.COLUMN_NAME_CONTENT + " DESC";

        Cursor cursor = db.query(
                FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        Log.i(TAG, "perfrom query data1:");
        while (cursor.moveToNext()) {
            long Id = cursor.getLong(cursor.getColumnIndexOrThrow(FeedEntry._ID));
            String date_temp = cursor.getString(cursor.getColumnIndex(FeedEntry.COLUMN_NAME_DATE));
            String state_temp = cursor.getString(cursor.getColumnIndex(FeedEntry.COLUMN_NAME_STATE));
            String context = cursor.getString(cursor.getColumnIndex(FeedEntry.COLUMN_NAME_CONTENT));
            Log.i(TAG, "ID:" + Id + ", DATE:" + date_temp + ", STATE:" + state_temp + ", context" + context);
            Note note = new Note(Id);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:MM:ss", Locale.ENGLISH);
            Date date = null;
            try {
                date = sdf.parse(date_temp);
            } catch (ParseException e) {
                e.printStackTrace();
                String s = e.getMessage();
                Log.i(TAG,s);
            }
            note.setDate(date);
            if("TODO".equals(state_temp)) {
                note.setState(State.from(0));
            }
            else {
                note.setState(State.from(1));
            }
            note.setContent(context);
            noteList.add(note);
        }

        cursor.close();
        Log.i(TAG,"close");
        return noteList;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        Log.i(TAG,"delete");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define 'where' part of query.
        String selection = FeedEntry._ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(note.id)};
        // Issue SQL statement.
        int deletedRows = db.delete(FeedEntry.TABLE_NAME, selection, selectionArgs);
        Log.i(TAG, "perform delennete data, result:" + deletedRows);
        notesAdapter.refresh(loadNotesFromDatabase());
    }

    private void updateNode(Note note) {
        // TODO 更新数据
        Log.i(TAG,"info1:   "+note.getContent()+"   date1:   "+note.getDate()+"     state1:    "+note.getState());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

// New value for one column
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_STATE, note.getState().toString());

// Which row to update
//String selection = FeedEntry.COLUMN_NAME_STATE+"!="+note.getState().toString();
        String selection = FeedEntry._ID + " LIKE ?";;
        String[] selectionArgs = {String.valueOf(note.id)};
        Log.i(TAG,selection);
        int count = db.update(
                FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        Log.i(TAG, "perform update data, result:" + count);
        notesAdapter.refresh(loadNotesFromDatabase());
        // 更新数据
    }

}
