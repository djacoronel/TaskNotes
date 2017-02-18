package com.djacoronel.tasknotes;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private ArrayList<Task> tasks;
    private TasksDbAdapter mDbHelper;
    private MethodCaller listener;

    RecyclerAdapter(ArrayList<Task> tasks, TasksDbAdapter mDbHelper, MethodCaller listener) {
        this.tasks = tasks;
        this.mDbHelper = mDbHelper;
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView task_title, task_text, task_due, task_priority;
        View priority_color;

        ViewHolder(View itemView) {
            super(itemView);
            task_title = (TextView) itemView.findViewById(R.id.task_title);
            task_text = (TextView) itemView.findViewById(R.id.task_text);
            task_due = (TextView) itemView.findViewById(R.id.task_day);
            task_priority = (TextView) itemView.findViewById(R.id.task_date);
            priority_color = itemView.findViewById(R.id.priority_color);

            itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.viewTask(getAdapterPosition());
                        }
                    }
            );
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (tasks.get(position).getTitle().isEmpty()) {
            holder.task_title.setText(tasks.get(position).getText());
            holder.task_text.setText("");
        } else {
            holder.task_title.setText(tasks.get(position).getTitle());
            holder.task_text.setText(tasks.get(position).getText());
        }

        if (!tasks.get(position).getDateFinished().isEmpty()) {
            holder.task_text.setText(tasks.get(position).getDateFinished());
            holder.task_text.setTextColor(Color.LTGRAY);
        }


        if (tasks.get(position).getDate().isEmpty()) {
            holder.task_due.setText("");
            holder.task_priority.setText("");
        } else {
            String date[] = tasks.get(position).getDate().split(" ");
            holder.task_due.setText(date[0]);
            holder.task_priority.setText(date[1]);
        }

        switch (tasks.get(position).getPriority()) {
            case "Priority: !":
                holder.priority_color.setBackgroundColor(Color.parseColor("#99CC00"));
                break;
            case "Priority: !!":
                holder.priority_color.setBackgroundColor(Color.parseColor("#FFBB32"));
                break;
            case "Priority: !!!":
                holder.priority_color.setBackgroundColor(Color.parseColor("#FF4444"));
                break;
            default:
                holder.priority_color.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        if (tasks == null) return 0;
        else return tasks.size();
    }

    interface MethodCaller {
        void viewTask(int position);

        void sortTasks();
    }

    void remove(int position, boolean archive) {
        if (archive) {
            mDbHelper.deleteTask(tasks.get(position).getId());
            tasks.remove(position);
            this.notifyItemRemoved(position);
        } else {
            mDbHelper.deleteArchivedTask(tasks.get(position).getId());
            tasks.remove(position);
            this.notifyItemRemoved(position);
        }

    }

    Task archive(int position) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("h:mma dd/MM/yyyy");
        String formattedDate = "Finished: " + df.format(c.getTime());

        Task task = tasks.get(position);
        long newId = mDbHelper.archiveTask(task.getId(), formattedDate);
        remove(position, true);
        task.setId(newId);

        task.setDateFinished(formattedDate);
        return task;
    }

    Task unarchive(int position) {
        Task task = tasks.get(position);
        long newId = mDbHelper.unarchiveTask(task.getId());
        remove(position, false);
        task.setDateFinished("");
        task.setId(newId);
        return task;
    }

    int add(String title, String text, String date, String priority, String tag) {
        Task task = new Task(mDbHelper.createTask(title, text, date, priority, tag), title, text, date, "", priority, tag);
        tasks.add(task);
        listener.sortTasks();
        this.notifyItemInserted(tasks.indexOf(task));
        return tasks.indexOf(task);
    }

    int update(int position, String title, String text, String date, String priority, String tag) {
        Task task = tasks.get(position);
        task.update(title, text, date, priority, tag);
        mDbHelper.updateTask(tasks.get(position).getId(), title, text, date, priority, tag);
        listener.sortTasks();
        this.notifyDataSetChanged();
        return tasks.indexOf(task);
    }


    Task tempTask;

    void onItemRemove(final int position, final RecyclerView recyclerView, final boolean archive) {
        tempTask = tasks.get(position);

        Snackbar snackbar = Snackbar
                .make(recyclerView, "TASK DELETED", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tasks.add(tempTask);
                        listener.sortTasks();
                        notifyItemInserted(tasks.indexOf(tempTask));
                        tempTask = null;
                    }
                }).addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int dismissType) {
                        super.onDismissed(snackbar, dismissType);

                        if (dismissType != DISMISS_EVENT_ACTION)
                            if (archive)
                                mDbHelper.deleteTask(tempTask.getId());
                            else
                                mDbHelper.deleteArchivedTask(tempTask.getId());

                    }
                });
        snackbar.show();
        tasks.remove(position);
        notifyItemRemoved(position);
    }
}
