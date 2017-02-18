package com.djacoronel.tasknotes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder>{

    LayoutInflater mInflater;
    ArrayList<Tag> mTagList;
    TasksDbAdapter mDbAdapter;
    MethodCaller lisetener;
    boolean isEditable;

    public TagAdapter(Context context, ArrayList<Tag> tags, MethodCaller lisetener, boolean isEditable){
        mInflater = LayoutInflater.from(context);
        mTagList = tags;
        mDbAdapter = new TasksDbAdapter(context);
        this.lisetener = lisetener;
        this.isEditable = isEditable;
    }

    public TagAdapter(Context context, ArrayList<Tag> tags, boolean isEditable){
        mInflater = LayoutInflater.from(context);
        mTagList = tags;
        mDbAdapter = new TasksDbAdapter(context);
        this.isEditable = isEditable;
    }

    @Override
    public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.tag_layout, parent, false);
        return new TagViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(TagViewHolder holder, int position) {
        holder.name.setText(mTagList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mTagList.size();
    }

    class TagViewHolder extends RecyclerView.ViewHolder{

        TextView name;
        ImageView button;

        public TagViewHolder(View itemView, TagAdapter adapter){
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.tag_name);
            button = (ImageView)itemView.findViewById(R.id.remove_tag_button);

            if(isEditable) {
                button.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Tag tag = mTagList.get(getAdapterPosition());
                                mDbAdapter.open();
                                mDbAdapter.deleteTag(tag.getId());
                                mTagList.remove(getAdapterPosition());
                                TagAdapter.this.notifyDataSetChanged();
                            }
                        }
                );
            } else {
                button.setVisibility(View.GONE);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lisetener.setTag(name.getText().toString());

                    }
                });
            }
        }
    }

    interface MethodCaller {
        void setTag(String tag);
    }
}
