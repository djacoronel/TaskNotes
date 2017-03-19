package com.djacoronel.tasknotes;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

class TagsTouchHelper extends ItemTouchHelper.SimpleCallback {
    private TagAdapter tagAdapter;

    TagsTouchHelper(TagAdapter tagAdapter) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT);
        this.tagAdapter = tagAdapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        tagAdapter.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition(), (TagAdapter.TagViewHolder) viewHolder);
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder holder) {
        return 0;
    }
}