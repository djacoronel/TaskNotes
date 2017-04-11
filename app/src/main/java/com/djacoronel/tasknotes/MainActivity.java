package com.djacoronel.tasknotes;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Tab1Fragment tab1;
    Tab2Fragment tab2;
    TasksDbAdapter mDbAdapter;
    NavigationView mNavView;
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.format_list_checks));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.checkbox_marked_circle_outline));

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        TabPagerAdapter adapter = new TabPagerAdapter(getFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);


        adapter.startUpdate(viewPager);
        tab1 = (Tab1Fragment) adapter.instantiateItem(viewPager, 0);
        tab2 = (Tab2Fragment) adapter.instantiateItem(viewPager, 1);
        adapter.finishUpdate(viewPager);

        tab1.setTab1Listener(new Tab1Fragment.Tab1Listener() {
            @Override
            public void refreshAdapter(Task task) {
                tab2.refresh(task);
            }
        });
        tab2.setTab1Listener(new Tab2Fragment.Tab2Listener() {
            @Override
            public void refreshAdapter(Task task) {
                tab1.refresh(task);
            }
        });

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tab1.addTask();
            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                if (tab.getPosition() == 1) {
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                    int fab_bottomMargin = layoutParams.bottomMargin;
                    fab.animate().translationY(fab.getHeight() + fab_bottomMargin).setInterpolator(new LinearInterpolator()).start();
                } else if (tab.getPosition() == 0) {
                    fab.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mDbAdapter = new TasksDbAdapter(this);
        mDbAdapter.open();

        populateTagMenu();

    }


    void populateTagMenu() {

        mNavView = (NavigationView) findViewById(R.id.nav_view);
        mNavView.setNavigationItemSelectedListener(this);

        menu = mNavView.getMenu();
        menu.removeGroup(R.id.group1);

        menu.add(R.id.group1, Menu.NONE, Menu.NONE, "All Tasks").setIcon(R.drawable.tag);

        Cursor mCursor = mDbAdapter.fetchTags();
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                long id = mCursor.getLong(mCursor.getColumnIndex("_id"));
                String name = mCursor.getString(mCursor.getColumnIndex("name"));
                String color = mCursor.getString(mCursor.getColumnIndex("color"));
                String pinned = mCursor.getString(mCursor.getColumnIndex("pinned"));

                menu.add(R.id.group1, Menu.NONE, Menu.NONE, name).setIcon(R.drawable.tag_outline);
            } while (mCursor.moveToNext());
        }

        menu.add(R.id.group1, Menu.NONE, Menu.NONE, "Edit Tags").setIcon(R.drawable.tag_plus);
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


        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout mdrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        String tag = (String) item.getTitle();

        if (tag.equals("Edit Tags")) {
            editTags();
            return true;
        } else {
            tab1.selectTag(tag);
            tab2.selectTag(tag);
            mdrawer.closeDrawer(GravityCompat.START);
            return true;
        }
    }

    public void showPinned(MenuItem item) {
        ImageView pinnedImage = (ImageView) findViewById(R.id.pinned_image);
        if (pinnedImage.getVisibility() == View.GONE) pinnedImage.setVisibility(View.VISIBLE);
        else pinnedImage.setVisibility(View.GONE);
    }


    public void editTags() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
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
        final TagAdapter mAdapter = new TagAdapter(this, tags, true);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(layoutManager);

        ItemTouchHelper.Callback callback = new TagsTouchHelper(mAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecycler);

        final EditText tagName = (EditText) mView.findViewById(R.id.add_tag_text);
        ImageView tagButton = (ImageView) mView.findViewById(R.id.add_tag_button);

        tagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = tagName.getText().toString();
                long id = mDbAdapter.createTag(name, "", "");
                tags.add(new Tag(id, name, "", ""));
                mAdapter.notifyDataSetChanged();
                layoutManager.smoothScrollToPosition(mRecycler, null, tags.size());
                tagName.setText("");

            }
        });

        final AlertDialog editTagsDialog = new AlertDialog
                .Builder(mView.getContext())
                .setView(mView)
                .setCancelable(true)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(Tag tag: tags){
                            mDbAdapter.updateTag(tag);
                        }

                        populateTagMenu();
                    }
                })
                .create();

        editTagsDialog.show();
    }
}

