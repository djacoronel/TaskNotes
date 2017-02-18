package com.djacoronel.tasknotes;


import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

class SwipeRightHelper extends ItemTouchHelper.SimpleCallback {
    private RecyclerAdapter adapter;
    private Tab1Fragment.Tab1Listener listener;

    SwipeRightHelper(RecyclerAdapter adapter, Tab1Fragment.Tab1Listener listener) {
        super(0, ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.listener = listener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        listener.refreshAdapter(adapter.archive(viewHolder.getAdapterPosition()));
    }
}
