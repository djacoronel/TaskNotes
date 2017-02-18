package com.djacoronel.tasknotes;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;


public class Tab1Fragment extends Fragment implements DatePickerDialog.OnDateSetListener, RecyclerAdapter.MethodCaller, TagAdapter.MethodCaller{

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerAdapter adapter;
    TasksDbAdapter mDbAdapter;
    RelativeLayout emptyScreen;

    ArrayList<Task> tasks = new ArrayList<>();

    public Tab1Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_tab1, container, false);

        emptyScreen = (RelativeLayout) rootView.findViewById(R.id.empty_screen);

        mDbAdapter = new TasksDbAdapter(rootView.getContext());
        mDbAdapter.open();

        fillData();

        layoutManager = new LinearLayoutManager(rootView.getContext()) {
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                LinearSmoothScroller smoothScroller = new LinearSmoothScroller(rootView.getContext()) {
                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return 75f / displayMetrics.densityDpi;
                    }
                };
                smoothScroller.setTargetPosition(position);
                startSmoothScroll(smoothScroller);
            }
        };

        adapter = new RecyclerAdapter(tasks, mDbAdapter, this);
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

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new SwipeRightHelper(adapter, listener);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        checkAdapterIsEmpty();

        return rootView;
    }

    private void fillData() {
        Cursor notesCursor = mDbAdapter.fetchAllTasks();

        if (notesCursor != null && notesCursor.moveToFirst()) {
            do {
                long id = notesCursor.getLong(notesCursor.getColumnIndex("_id"));
                String title = notesCursor.getString(notesCursor.getColumnIndex("title"));
                String text = notesCursor.getString(notesCursor.getColumnIndex("body"));
                String date = notesCursor.getString(notesCursor.getColumnIndex("date"));
                String tag = notesCursor.getString(notesCursor.getColumnIndex("tag"));
                String dateFinished = "";

                String priority = notesCursor.getString(notesCursor.getColumnIndex("priority"));


                tasks.add(new Task(id, title, text, date, dateFinished, priority, tag));
            } while (notesCursor.moveToNext());

            sortTasks();
        }
    }

    public void checkAdapterIsEmpty() {
        if (tasks.isEmpty()) {
            emptyScreen.setVisibility(View.VISIBLE);
        } else {
            emptyScreen.setVisibility(View.GONE);
        }
    }

    public void sortTasks() {
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {

                int returnVal = 0;

                if (!t1.getDate().isEmpty() && !t2.getDate().isEmpty()) {
                    String dayDate1[] = t1.getDate().split(" ");
                    String date1[] = dayDate1[1].split("/");

                    String dayDate2[] = t2.getDate().split(" ");
                    String date2[] = dayDate2[1].split("/");

                    if (Integer.parseInt(date1[0]) < Integer.parseInt(date2[0])) {
                        returnVal = -1;
                    } else if (Integer.parseInt(date1[0]) > Integer.parseInt(date2[0])) {
                        returnVal = 1;
                    } else if (Integer.parseInt(date1[0]) == Integer.parseInt(date2[0])) {
                        if (Integer.parseInt(date1[1]) < Integer.parseInt(date2[1])) {
                            returnVal = -1;
                        } else if (Integer.parseInt(date1[1]) > Integer.parseInt(date2[1])) {
                            returnVal = 1;
                        } else if (Integer.parseInt(date1[1]) == Integer.parseInt(date2[1])) {
                            if (!t1.getPriority().isEmpty() && !t2.getPriority().isEmpty()) {
                                returnVal = -t1.getPriority().compareTo(t2.getPriority());
                            } else if (t1.getPriority().isEmpty() && !t2.getPriority().isEmpty()) {
                                returnVal = 1;
                            } else if (!t1.getPriority().isEmpty() && t2.getPriority().isEmpty()) {
                                returnVal = -1;
                            }
                        }
                    }
                } else if (!t1.getDate().isEmpty() && t2.getDate().isEmpty()) {
                    returnVal = -1;
                } else if (t1.getDate().isEmpty() && !t2.getDate().isEmpty()) {
                    returnVal = 1;
                } else if (t1.getDate().isEmpty() && t2.getDate().isEmpty()) {
                    if (!t1.getPriority().isEmpty() && !t2.getPriority().isEmpty()) {
                        returnVal = -t1.getPriority().compareTo(t2.getPriority());
                    } else if (t1.getPriority().isEmpty() && !t2.getPriority().isEmpty()) {
                        returnVal = 1;
                    } else if (!t1.getPriority().isEmpty() && t2.getPriority().isEmpty()) {
                        returnVal = -1;
                    }
                }
                return returnVal;
            }
        });
    }

    EditText editTitle, editText;
    TextView setDue, setPriority, setTag;
    String selectedTag = "All Tasks";

    public void addTask() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity());
        final View mView = layoutInflaterAndroid.inflate(R.layout.add_task_layout, null);

        editTitle = (EditText) mView.findViewById(R.id.edit_title);
        editText = (EditText) mView.findViewById(R.id.edit_text);
        setDue = (TextView) mView.findViewById(R.id.set_due);
        setPriority = (TextView) mView.findViewById(R.id.set_priority);
        setTag = (TextView) mView.findViewById(R.id.set_tag);

        if(!selectedTag.equals("All Tasks"))
            setTag.setText(selectedTag);

        setDue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setDue();
                return false;
            }
        });
        setPriority.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setPriority();
                return false;
            }
        });
        setTag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setTag();
                return false;
            }
        });

        final AlertDialog addTaskDialog = new AlertDialog
                .Builder(mView.getContext())
                .setView(mView)
                .setCancelable(true)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        dialogBox.cancel();
                    }
                })
                .setPositiveButton("Add", null)
                .create();

        addTaskDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        addTaskDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button buttonPositive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                buttonPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = editTitle.getText().toString();
                        String text = editText.getText().toString();
                        String due;
                        String priority;
                        String tag;

                        if (!title.isEmpty() || !text.isEmpty()) {
                            if (!setDue.getText().toString().equals("Set Due"))
                                due = setDue.getText().toString();
                            else
                                due = "";
                            if (!setPriority.getText().toString().trim().equals("Set Priority"))
                                priority = setPriority.getText().toString();
                            else
                                priority = "";
                            if (!setTag.getText().toString().trim().equals("Set Tag"))
                                tag = setTag.getText().toString();
                            else
                                tag = "";

                            layoutManager.smoothScrollToPosition(recyclerView, null, adapter.add(title, text, due, priority, tag));
                            addTaskDialog.dismiss();
                        } else {
                            final Snackbar snackbar = Snackbar.make(mView, "ADD TASK CONTENT", Snackbar.LENGTH_LONG);
                            snackbar.setAction("OKAY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }
                    }
                });
            }
        });

        addTaskDialog.show();
    }

    public void viewTask(final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity());
        View mView = layoutInflaterAndroid.inflate(R.layout.view_task_layout, null);


        editTitle = (EditText) mView.findViewById(R.id.edit_title);
        editText = (EditText) mView.findViewById(R.id.edit_text);
        setDue = (TextView) mView.findViewById(R.id.set_due);
        setTag = (TextView) mView.findViewById(R.id.set_tag);
        setPriority = (TextView) mView.findViewById(R.id.set_priority);

        final Task task = tasks.get(position);

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

        final AlertDialog viewTaskDialog = new AlertDialog
                .Builder(mView.getContext())
                .setView(mView)
                .setCancelable(true)
                .setPositiveButton("Edit", null)
                .setNegativeButton("Close", null)
                .setNeutralButton("Delete", null)
                .create();

        viewTaskDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                final Button buttonPositive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                final Button buttonNegative = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                final Button buttonNeutral = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);

                buttonNeutral.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.onItemRemove(position, recyclerView, true);
                        viewTaskDialog.dismiss();
                    }
                });

                buttonNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (buttonPositive.getText().equals("Save")) {
                            setDue.setOnTouchListener(null);
                            setPriority.setOnTouchListener(null);

                            editTitle.clearFocus();
                            editText.clearFocus();

                            editTitle.setText(tasks.get(position).getTitle());
                            editText.setText(tasks.get(position).getText());

                            if (tasks.get(position).getDate().isEmpty())
                                setDue.setText("Set Due");
                            else
                                setDue.setText(tasks.get(position).getDate());

                            if (tasks.get(position).getPriority().isEmpty())
                                setPriority.setText("Set Priority");
                            else
                                setPriority.setText(tasks.get(position).getPriority());

                            if(tasks.get(position).getTag().isEmpty())
                                setTag.setText("Set Tag");
                            else
                                setTag.setText(tasks.get(position).getTag());

                            if (setDue.getText().toString().equals("Set Due"))
                                setDue.setVisibility(View.GONE);
                            if (setPriority.getText().toString().equals("Set Priority"))
                                setPriority.setVisibility(View.GONE);
                            if (setTag.getText().toString().equals("Set Tag"))
                                setTag.setVisibility(View.GONE);

                            if (editTitle.getText().toString().isEmpty())
                                editTitle.setVisibility(View.GONE);
                            if (editText.getText().toString().isEmpty())
                                editText.setVisibility(View.GONE);

                            editTitle.setFocusable(false);
                            editText.setFocusable(false);

                            buttonPositive.setText("Edit");
                            buttonNegative.setText(("Close"));
                        } else {
                            viewTaskDialog.dismiss();
                        }
                    }
                });

                buttonPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (buttonPositive.getText().equals("Edit")) {

                            editTitle.setFocusableInTouchMode(true);
                            editText.setFocusableInTouchMode(true);

                            editTitle.setVisibility(View.VISIBLE);
                            editText.setVisibility(View.VISIBLE);

                            editText.requestFocus();

                            if (setDue.getVisibility() == View.GONE) {
                                setDue.setText("Set Due");
                                setDue.setVisibility(View.VISIBLE);
                            }
                            if (setPriority.getVisibility() == View.GONE) {
                                setPriority.setText("Set Priority");
                                setPriority.setVisibility(View.VISIBLE);
                            }
                            if (setTag.getVisibility() == View.GONE) {
                                setTag.setText("Set Tag");
                                setTag.setVisibility(View.VISIBLE);
                            }

                            setDue.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    setDue();
                                    return false;
                                }
                            });

                            setPriority.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    setPriority();
                                    return false;
                                }
                            });
                            setTag.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    setTag();
                                    return false;
                                }
                            });

                            buttonPositive.setText("Save");
                            buttonNegative.setText("Cancel");

                        } else {

                            setDue.setOnTouchListener(null);
                            setPriority.setOnTouchListener(null);
                            setTag.setOnTouchListener(null);

                            editTitle.clearFocus();
                            editText.clearFocus();

                            String date = "", priority = "", tag = "";

                            if (!setDue.getText().toString().equals("Set Due"))
                                date = setDue.getText().toString();
                            else
                                setDue.setVisibility(View.GONE);

                            if (!setPriority.getText().toString().equals("Set Priority"))
                                priority = setPriority.getText().toString();
                            else
                                setPriority.setVisibility(View.GONE);

                            if(!setTag.getText().toString().equals("Set Tag"))
                                tag = setTag.getText().toString();
                            else
                                setTag.setVisibility(View.GONE);

                            if (editTitle.getText().toString().isEmpty())
                                editTitle.setVisibility(View.GONE);
                            if (editText.getText().toString().isEmpty())
                                editText.setVisibility(View.GONE);

                            adapter.update(position, editTitle.getText().toString(), editText.getText().toString(), date, priority, tag);
                            adapter.notifyItemChanged(position);

                            editTitle.setFocusable(false);
                            editText.setFocusable(false);

                            buttonPositive.setText("Edit");
                            buttonNegative.setText(("Close"));

                        }
                    }
                });
            }
        });
        viewTaskDialog.show();
    }

    public void setPriority() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity());
        View mView = layoutInflaterAndroid.inflate(R.layout.set_priority_layout, null);

        View priority1 = mView.findViewById(R.id.priority_1);
        View priority2 = mView.findViewById(R.id.priority_2);
        View priority3 = mView.findViewById(R.id.priority_3);

        final AlertDialog viewDialog = new AlertDialog
                .Builder(mView.getContext())
                .setView(mView)
                .setCancelable(true)
                .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setPriority.setText("Set Priority");
                    }
                })
                .create();
        viewDialog.show();

        priority1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPriority.setText("Priority: !");
                viewDialog.dismiss();
            }
        });

        priority2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPriority.setText("Priority: !!");
                viewDialog.dismiss();
            }
        });

        priority3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPriority.setText("Priority: !!!");
                viewDialog.dismiss();
            }
        });
    }

    public void setDue() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                Tab1Fragment.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setVersion(DatePickerDialog.Version.VERSION_2);
        dpd.setCancelText("Clear");
        dpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                setDue.setText("Set Due");
            }
        });
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        String dayOfWeek;

        Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        int result = calendar.get(Calendar.DAY_OF_WEEK);
        switch (result) {
            case Calendar.SUNDAY:
                dayOfWeek = "Sunday";
                break;
            case Calendar.MONDAY:
                dayOfWeek = "Monday";
                break;
            case Calendar.TUESDAY:
                dayOfWeek = "Tuesday";
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = "Wednesday";
                break;
            case Calendar.THURSDAY:
                dayOfWeek = "Thursday";
                break;
            case Calendar.FRIDAY:
                dayOfWeek = "Friday";
                break;
            case Calendar.SATURDAY:
                dayOfWeek = "Saturday";
                break;
            default:
                dayOfWeek = "";
        }
        setDue.setText(dayOfWeek + " " + (monthOfYear + 1) + "/" + dayOfMonth);
    }

    Tab1Listener listener;

    public interface Tab1Listener {
        void refreshAdapter(Task task);
    }

    public void setTab1Listener(Tab1Listener listener) {
        this.listener = listener;
    }

    public void refresh(Task task) {
        tasks.add(task);
        sortTasks();
        adapter.notifyDataSetChanged();
        layoutManager.scrollToPosition(tasks.indexOf(task));

    }

    public void selectTag(String selectedTag){
        this.selectedTag = selectedTag;
        Cursor notesCursor = mDbAdapter.fetchAllTasks();
        tasks.clear();

        if (notesCursor != null && notesCursor.moveToFirst()) {
            do {
                long id = notesCursor.getLong(notesCursor.getColumnIndex("_id"));
                String title = notesCursor.getString(notesCursor.getColumnIndex("title"));
                String text = notesCursor.getString(notesCursor.getColumnIndex("body"));
                String date = notesCursor.getString(notesCursor.getColumnIndex("date"));
                String priority = notesCursor.getString(notesCursor.getColumnIndex("priority"));
                String tag = notesCursor.getString(notesCursor.getColumnIndex("tag"));


                if(selectedTag.equals("All Tasks")){
                    if(adapter.tempTask == null || id != adapter.tempTask.getId())
                        tasks.add(new Task(id, title, text, date, "", priority, tag));
                } else {
                    if(tag.equals(selectedTag))
                        if(adapter.tempTask == null || id != adapter.tempTask.getId())
                            tasks.add(new Task(id, title, text, date, "", priority, tag));
                }

            } while (notesCursor.moveToNext());

            sortTasks();
            adapter.notifyDataSetChanged();
        }
    }

    AlertDialog editTagsDialog;

    public void setTag(String tag){
        setTag.setText(tag);
        editTagsDialog.dismiss();
    }

    public void setTag(){
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity());
        View mView = layoutInflaterAndroid.inflate(R.layout.edit_tags_layout, null);

        final ArrayList<Tag> tags = new ArrayList<>();

        Cursor mCursor = mDbAdapter.fetchTags();
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                long id = mCursor.getLong(mCursor.getColumnIndex("_id"));
                String name = mCursor.getString(mCursor.getColumnIndex("name"));
                String color = mCursor.getString(mCursor.getColumnIndex("color"));
                String pinned = mCursor.getString(mCursor.getColumnIndex("pinned"));

                tags.add(new Tag(id, name, color, pinned));
            } while (mCursor.moveToNext());
        }

        final RecyclerView mRecycler = (RecyclerView) mView.findViewById(R.id.edit_tags_recycler);
        final TagAdapter mAdapter = new TagAdapter(getActivity(), tags, this, false);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(layoutManager);

        EditText tagName = (EditText) mView.findViewById(R.id.add_tag_text);
        ImageView tagButton = (ImageView) mView.findViewById(R.id.add_tag_button);

        tagName.setVisibility(View.GONE);
        tagButton.setVisibility(View.GONE);


        editTagsDialog = new AlertDialog
                .Builder(mView.getContext())
                .setView(mView)
                .setCancelable(true)
                .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setTag.setText("Set Tag");
                    }
                })
                .create();

        editTagsDialog.show();
    }
}
