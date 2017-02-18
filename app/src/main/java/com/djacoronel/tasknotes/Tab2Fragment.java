package com.djacoronel.tasknotes;


import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class Tab2Fragment extends Fragment implements RecyclerAdapter.MethodCaller {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerAdapter adapter;
    TasksDbAdapter mDbHelper;
    RelativeLayout emptyScreen;

    ArrayList<Task> tasks = new ArrayList<>();

    public Tab2Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_tab2, container, false);

        emptyScreen = (RelativeLayout) rootView.findViewById(R.id.empty_screen);

        mDbHelper = new TasksDbAdapter(rootView.getContext());
        mDbHelper.open();

        fillData();

        adapter = new RecyclerAdapter(tasks, mDbHelper, this);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkAdapterIsEmpty();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                checkAdapterIsEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkAdapterIsEmpty();
            }
        });

        layoutManager = new LinearLayoutManager(rootView.getContext());

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        checkAdapterIsEmpty();

        return rootView;
    }


    public void fillData() {
        Cursor archiveCursor = mDbHelper.fetchArchivedTasks();

        if (archiveCursor != null && archiveCursor.moveToFirst()) {
            archiveCursor.moveToLast();
            do {
                long id = archiveCursor.getLong(archiveCursor.getColumnIndex("_id"));
                String title = archiveCursor.getString(archiveCursor.getColumnIndex("title"));
                String text = archiveCursor.getString(archiveCursor.getColumnIndex("body"));
                String date = archiveCursor.getString(archiveCursor.getColumnIndex("date"));
                String priority = archiveCursor.getString(archiveCursor.getColumnIndex("priority"));
                String tag = archiveCursor.getString(archiveCursor.getColumnIndex("tag"));
                String dateFinished = archiveCursor.getString(archiveCursor.getColumnIndex("datefinished"));

                tasks.add(new Task(id, title, text, date, dateFinished, priority, tag));
            } while (archiveCursor.moveToPrevious());
        }
    }

    public void viewTask(final int position) {
        EditText editTitle, editText;
        TextView setDue, setPriority, setTag;

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity());
        View mView = layoutInflaterAndroid.inflate(R.layout.view_task_layout, null);

        editTitle = (EditText) mView.findViewById(R.id.edit_title);
        editText = (EditText) mView.findViewById(R.id.edit_text);
        setDue = (TextView) mView.findViewById(R.id.set_due);
        setPriority = (TextView) mView.findViewById(R.id.set_priority);
        setTag = (TextView) mView.findViewById(R.id.set_tag);

        Task task = tasks.get(position);

        setDue.setText(task.getDate());
        setPriority.setText(task.getPriority());
        setTag.setText(task.getTag());

        editTitle.setText(task.getTitle());
        editText.setText(task.getText());

        editTitle.setFocusable(false);
        editText.setFocusable(false);

        if (setDue.getText().toString().isEmpty()) setDue.setVisibility(View.GONE);
        if (setPriority.getText().toString().isEmpty()) setPriority.setVisibility(View.GONE);
        if (setTag.getText().toString().isEmpty()) setTag.setVisibility(View.GONE);
        if (editTitle.getText().toString().isEmpty()) editTitle.setVisibility(View.GONE);
        if (editText.getText().toString().isEmpty()) editText.setVisibility(View.GONE);

        final AlertDialog viewDialog = new AlertDialog
                .Builder(mView.getContext())
                .setView(mView)
                .setCancelable(true)
                .setPositiveButton("Close", null)
                .setNeutralButton("Move to tasks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.refreshAdapter(adapter.unarchive(position));
                    }
                })
                .create();

        viewDialog.show();
    }

    public void checkAdapterIsEmpty() {
        if (tasks.isEmpty()) {
            emptyScreen.setVisibility(View.VISIBLE);
        } else {
            emptyScreen.setVisibility(View.GONE);
        }
    }

    public void sortTasks() {

    }

    public void refresh(Task task) {
        tasks.add(0, task);
        adapter.notifyItemInserted(0);
        layoutManager.scrollToPosition(0);
    }

    Tab2Listener listener;

    public interface Tab2Listener {
        void refreshAdapter(Task task);
    }

    public void setTab1Listener(Tab2Listener listener) {
        this.listener = listener;
    }

    public void selectTag(String selectedTag){
        Cursor notesCursor = mDbHelper.fetchArchivedTasks();
        tasks.clear();

        if (notesCursor != null && notesCursor.moveToFirst()) {
            do {
                long id = notesCursor.getLong(notesCursor.getColumnIndex("_id"));
                String title = notesCursor.getString(notesCursor.getColumnIndex("title"));
                String text = notesCursor.getString(notesCursor.getColumnIndex("body"));
                String date = notesCursor.getString(notesCursor.getColumnIndex("date"));
                String priority = notesCursor.getString(notesCursor.getColumnIndex("priority"));
                String tag = notesCursor.getString(notesCursor.getColumnIndex("tag"));
                String dateFinished = notesCursor.getString(notesCursor.getColumnIndex("datefinished"));



                if(selectedTag.equals("All Tasks")){
                    if(adapter.tempTask == null || id != adapter.tempTask.getId())
                        tasks.add(new Task(id, title, text, date, dateFinished, priority, tag));
                } else {
                    if(tag.equals(selectedTag))
                        if(adapter.tempTask == null || id != adapter.tempTask.getId())
                            tasks.add(new Task(id, title, text, date, dateFinished, priority, tag));
                }

            } while (notesCursor.moveToNext());

            sortTasks();
            adapter.notifyDataSetChanged();
        }
    }
}
