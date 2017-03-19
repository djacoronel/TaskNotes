package com.djacoronel.tasknotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TasksDbAdapter {

    private static final String KEY_TITLE = "title";
    private static final String KEY_PRIORITY = "priority";
    private static final String KEY_DATE = "date";
    private static final String KEY_BODY = "body";
    private static final String KEY_FINISH = "datefinished";
    private static final String KEY_ROWID = "_id";
    private static final String KEY_TAG = "tag";

    private static final String KEY_NAME = "name";
    private static final String KEY_COLOR = "color";
    private static final String KEY_PINNED = "pinned";

    private static final String TAG = "TasksDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mCtx;

    private static final String DATABASE_CREATE =
            "create table pendingTasks (_id integer primary key autoincrement, "
                    + "title text not null, body text not null, "
                    + "date text not null, priority text not null, tag text not null);";
    private static final String DATABASE_ARCHIVE_CREATE =
            "create table finishedTasks (_id integer primary key autoincrement, "
                    + "title text not null, body text not null, "
                    + "date text not null, datefinished text not null, "
                    + "priority text not null, tag text not null);";
    private static final String DATABASE_TAGS_CREATE =
            "create table tags (_id integer primary key autoincrement, " +
                    "name text not null, color text not null, pinned text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "pendingTasks";
    private static final String DATABASE_ARCHIVE_TABLE = "finishedTasks";
    private static final String DATABASE_TAGS_TABLE = "tags";

    private static final int DATABASE_VERSION = 2;

    TasksDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
            db.execSQL(DATABASE_ARCHIVE_CREATE);
            db.execSQL(DATABASE_TAGS_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    public TasksDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    long createTask(String title, String body, String date, String priority, String tag) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_PRIORITY, priority);
        initialValues.put(KEY_TAG, tag);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    boolean deleteTask(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    Cursor fetchAllTasks() {

        return mDb.query(DATABASE_TABLE, new String[]{KEY_ROWID, KEY_TITLE,
                KEY_BODY, KEY_DATE, KEY_PRIORITY, KEY_TAG}, null, null, null, null, null);
    }

    Cursor fetchTask(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[]{
                        KEY_ROWID, KEY_TITLE, KEY_BODY, KEY_DATE, KEY_PRIORITY, KEY_TAG}, KEY_ROWID
                        + "=" + rowId, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    boolean updateTask(long rowId, String title, String body, String date, String priority, String tag) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);
        args.put(KEY_DATE, date);
        args.put(KEY_PRIORITY, priority);
        args.put(KEY_TAG, tag);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    Cursor fetchArchivedTasks() {
        return mDb.query(DATABASE_ARCHIVE_TABLE, new String[]{KEY_ROWID, KEY_TITLE,
                KEY_BODY, KEY_DATE, KEY_FINISH, KEY_PRIORITY, KEY_TAG}, null, null, null, null, null);
    }

    Cursor fetchArchivedTask(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_ARCHIVE_TABLE, new String[]{
                        KEY_ROWID, KEY_TITLE, KEY_BODY, KEY_DATE, KEY_FINISH, KEY_PRIORITY, KEY_TAG}, KEY_ROWID
                        + "=" + rowId, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    boolean deleteArchivedTask(long rowId) {

        return mDb.delete(DATABASE_ARCHIVE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    long archiveTask(long rowId, String datefinished) {
        Cursor mCursor = fetchTask(rowId);

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, mCursor.getString(mCursor.getColumnIndex("title")));
        initialValues.put(KEY_BODY, mCursor.getString(mCursor.getColumnIndex("body")));
        initialValues.put(KEY_DATE, mCursor.getString(mCursor.getColumnIndex("date")));
        initialValues.put(KEY_FINISH, datefinished);
        initialValues.put(KEY_PRIORITY, mCursor.getString(mCursor.getColumnIndex("priority")));
        initialValues.put(KEY_TAG, mCursor.getString(mCursor.getColumnIndex("tag")));

        return mDb.insert(DATABASE_ARCHIVE_TABLE, null, initialValues);
    }

    long unarchiveTask(long rowId) {
        Cursor mCursor = fetchArchivedTask(rowId);

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, mCursor.getString(mCursor.getColumnIndex("title")));
        initialValues.put(KEY_BODY, mCursor.getString(mCursor.getColumnIndex("body")));
        initialValues.put(KEY_DATE, mCursor.getString(mCursor.getColumnIndex("date")));
        initialValues.put(KEY_PRIORITY, mCursor.getString(mCursor.getColumnIndex("priority")));
        initialValues.put(KEY_TAG, mCursor.getString(mCursor.getColumnIndex("tag")));

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    long createTag(String name, String color, String pinned) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_COLOR, color);
        initialValues.put(KEY_PINNED, pinned);

        return mDb.insert(DATABASE_TAGS_TABLE, null, initialValues);
    }

    void updateTag(Tag tag){
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, tag.getName());
        values.put(KEY_COLOR, tag.getColor());
        values.put(KEY_PINNED, tag.getPinned());

        String selection = KEY_ROWID + " LIKE ?";
        String[] selectionArgs = { "" + tag.getId() };

        mDb.update(
                DATABASE_TAGS_TABLE,
                values,
                selection,
                selectionArgs);
    }

    boolean deleteTag(long rowId) {
        return mDb.delete(DATABASE_TAGS_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    Cursor fetchTags(){
        return mDb.query(DATABASE_TAGS_TABLE, new String[]{KEY_ROWID, KEY_NAME, KEY_COLOR,
                KEY_PINNED}, null, null, null, null, null);
    }

    void createTagsTable(){
        mDb.execSQL(DATABASE_TAGS_CREATE);
    }

    void migrateData(){
        mDb.execSQL("create table pendingTasks (_id integer primary key autoincrement, " +
                "title text not null, body text not null, " +
                "date text not null, priority text not null," +
                " tag text not null);");
        mDb.execSQL("create table finishedTasks (_id integer primary key autoincrement, " +
                "title text not null, body text not null, " +
                "date text not null, datefinished text not null, " +
                "priority text not null, tag text not null);");

        Cursor mCursor = fetchAllTasks();

        if (mCursor != null && (mCursor.moveToFirst())) {
            do {
                long id = (mCursor.getLong(mCursor.getColumnIndex("_id")));
                String title = (mCursor.getString(mCursor.getColumnIndex("title")));
                String text = (mCursor.getString(mCursor.getColumnIndex("body")));
                String date = (mCursor.getString(mCursor.getColumnIndex("date")));
                String priority = (mCursor.getString(mCursor.getColumnIndex("priority")));

                createTask(title, text, date, priority, "");
            } while (mCursor.moveToNext());
        }

        mCursor = fetchArchivedTasks();

        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                long id = mCursor.getLong(mCursor.getColumnIndex("_id"));
                String title = mCursor.getString(mCursor.getColumnIndex("title"));
                String text = mCursor.getString(mCursor.getColumnIndex("body"));
                String date = mCursor.getString(mCursor.getColumnIndex("date"));
                String priority = mCursor.getString(mCursor.getColumnIndex("priority"));
                String dateFinished = mCursor.getString(mCursor.getColumnIndex("datefinished"));


                ContentValues initialValues = new ContentValues();
                initialValues.put(KEY_TITLE, title);
                initialValues.put(KEY_BODY, text);
                initialValues.put(KEY_DATE, date);
                initialValues.put(KEY_FINISH, dateFinished);
                initialValues.put(KEY_PRIORITY, priority);
                initialValues.put(KEY_TAG, "");

                mDb.insert(DATABASE_ARCHIVE_TABLE, null, initialValues);
            } while (mCursor.moveToNext());
        }
    }
}