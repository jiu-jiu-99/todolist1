package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.db.FeedReaderContract;
import com.byted.camp.todolist.operation.db.FeedReaderDbHelper;

import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.beans.Note;

import java.sql.Date;
import java.text.ChoiceFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class NoteActivity extends AppCompatActivity {

    private static final String TAG = "NoteActivity";
    private EditText editText;
    private Button addBtn;

    private TodoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle(R.string.take_a_note);

        editText = findViewById(R.id.edit_text);
        editText.setFocusable(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }
        dbHelper = new TodoDbHelper(this);
        addBtn = findViewById(R.id.btn_add);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editText.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(NoteActivity.this,
                            "No content to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean succeed = saveNote2Database(content.toString().trim());
                if (succeed) {
                    Toast.makeText(NoteActivity.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(NoteActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    private boolean saveNote2Database(String content) {
        // TODO 插入一条新数据，返回是否插入成功

        Log.i(TAG, "perform add data, result:");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        State state = State.from(0);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:MM:ss", Locale.ENGLISH);
        Date date = new Date(System.currentTimeMillis());

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(TodoContract.FeedEntry.COLUMN_NAME_DATE, simpleDateFormat.format(date));
        values.put(TodoContract.FeedEntry.COLUMN_NAME_STATE, state.toString());
        values.put(TodoContract.FeedEntry.COLUMN_NAME_CONTENT, content);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(TodoContract.FeedEntry.TABLE_NAME, null, values);
        Log.i(TAG, "perform add data, result:" + newRowId);
        return newRowId != -1;
    }
}
