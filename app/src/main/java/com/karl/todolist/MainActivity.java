package com.karl.todolist;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.karl.todolist.db.TaskContract;
import com.karl.todolist.db.TaskDbHelper;

import java.util.ArrayList;

/*
 * Done mostly from following the tutorial at
 * https://www.sitepoint.com/starting-android-development-creating-todo-app/
 */
public class MainActivity extends AppCompatActivity {

    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHelper = new TaskDbHelper(this);
        mTaskListView = findViewById(R.id.todo_list);

        // create fab for adding tasks
        FloatingActionButton fab = findViewById(R.id.action_add_task);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final EditText taskEditText = new EditText(view.getContext());

                // create pop up prompting for new task
                AlertDialog dialog = new AlertDialog.Builder(view.getContext())
                        .setTitle("Add a new task")
                        .setMessage("What do you want to do next?")
                        .setView(taskEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // add task to database and update UI
                                String task = String.valueOf(taskEditText.getText());
                                SQLiteDatabase db = mHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                                db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                                        null,
                                        values,
                                        SQLiteDatabase.CONFLICT_REPLACE);
                                db.close();
                                updateUI();
                            }
                        })

                        .setNegativeButton("Cancel", null)
                        .create();

                dialog.show();
            } // end of onClick

        }); // end of set listener

        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_task) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // deletes a task from the database and updates the UT
    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = mHelper.getWritableDatabase();

        // delete task
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});
        db.close();
        updateUI();

        // show popup at bottom telling user which task was completed
        Snackbar.make(view, "'" + task + "' completed", Snackbar.LENGTH_LONG).show();
    }

    // fetches list of tasks from the db and updates UT with new task list
    private void updateUI() {
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();

        // get tasks from database with cursor
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idx));
        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.todo_item,
                    R.id.task_title,
                    taskList);
            mTaskListView.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }


}
