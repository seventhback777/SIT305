package com.example.lostandfoundapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.lostandfoundapp.model.Post;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "lostandfound.db";
    private static final int DB_VERSION = 2;
    private static final String TABLE_POSTS = "posts";

    public static final String COL_ID = "id";
    public static final String COL_POST_TYPE = "post_type";
    public static final String COL_NAME = "name";
    public static final String COL_PHONE = "phone";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DATE = "date";
    public static final String COL_LOCATION = "location";
    public static final String COL_CATEGORY = "category";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_IMAGE_PATH = "image_path";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_POSTS + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_POST_TYPE + " TEXT NOT NULL, " +
            COL_NAME + " TEXT NOT NULL, " +
            COL_PHONE + " TEXT NOT NULL, " +
            COL_DESCRIPTION + " TEXT NOT NULL, " +
            COL_DATE + " TEXT NOT NULL, " +
            COL_LOCATION + " TEXT NOT NULL, " +
            COL_CATEGORY + " TEXT NOT NULL, " +
            COL_TIMESTAMP + " TEXT NOT NULL, " +
            COL_IMAGE_PATH + " TEXT, " +
            COL_LATITUDE + " REAL DEFAULT 0, " +
            COL_LONGITUDE + " REAL DEFAULT 0" +
            ")";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_POSTS + " ADD COLUMN " + COL_LATITUDE + " REAL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_POSTS + " ADD COLUMN " + COL_LONGITUDE + " REAL DEFAULT 0");
        }
    }

    public long insertPost(Post post) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_POST_TYPE, post.getPostType());
        cv.put(COL_NAME, post.getName());
        cv.put(COL_PHONE, post.getPhone());
        cv.put(COL_DESCRIPTION, post.getDescription());
        cv.put(COL_DATE, post.getDate());
        cv.put(COL_LOCATION, post.getLocation());
        cv.put(COL_CATEGORY, post.getCategory());
        cv.put(COL_TIMESTAMP, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        cv.put(COL_IMAGE_PATH, post.getImagePath());
        cv.put(COL_LATITUDE, post.getLatitude());
        cv.put(COL_LONGITUDE, post.getLongitude());
        return db.insert(TABLE_POSTS, null, cv);
    }

    public List<Post> getAllPosts() {
        return queryPosts(null, null);
    }

    public List<Post> getPostsByCategory(String category) {
        return queryPosts(COL_CATEGORY + " = ?", new String[]{category});
    }

    public Post getPostById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_POSTS, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Post post = null;
        if (cursor.moveToFirst()) {
            post = cursorToPost(cursor);
        }
        cursor.close();
        return post;
    }

    public void deletePost(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_POSTS, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    private List<Post> queryPosts(String selection, String[] selectionArgs) {
        List<Post> posts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_POSTS, null, selection, selectionArgs,
                null, null, COL_TIMESTAMP + " DESC");
        if (cursor.moveToFirst()) {
            do {
                posts.add(cursorToPost(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return posts;
    }

    private Post cursorToPost(Cursor cursor) {
        Post post = new Post();
        post.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        post.setPostType(cursor.getString(cursor.getColumnIndexOrThrow(COL_POST_TYPE)));
        post.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
        post.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)));
        post.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
        post.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)));
        post.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION)));
        post.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)));
        post.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)));
        post.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_PATH)));
        int latIdx = cursor.getColumnIndex(COL_LATITUDE);
        int lngIdx = cursor.getColumnIndex(COL_LONGITUDE);
        if (latIdx >= 0) post.setLatitude(cursor.getDouble(latIdx));
        if (lngIdx >= 0) post.setLongitude(cursor.getDouble(lngIdx));
        return post;
    }
}
