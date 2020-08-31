package edu.inha.hellocookieya.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import timber.log.Timber;

class AppDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "HelloCookieYa.db";

    /*
        CREATE TABLE IF NOT EXISTS playlist (
            _id INTEGER PRIMARY KEY,
            playlist_name TEXT)
    */
    private static final String SQL_CREATE_PLAYLIST_ENTRY =
            "CREATE TABLE IF NOT EXISTS " + AppDBContract.PlayListEntry.TABLE_NAME + "  (" +
                    AppDBContract.PlayListEntry._ID + " INTEGER PRIMARY KEY, " +
                    AppDBContract.PlayListEntry.COLUMN_NAME_PLAYLIST_NAME + " TEXT)";

    /*
        CREATE TABLE IF NOT EXISTS link_video (
            _id INTEGER PRIMARY KEY,
            video_youtube_id TEXT,
            title TEXT,
            url TEXT,
            description TEXT,
            playlist_id INTEGER REFERENCES playlist(_id) ON UPDATE CASCADE ON DELETE CASCADE)
    */
    private static final String SQL_CREATE_LINK_VIDEO_ENTRY =
            "CREATE TABLE IF NOT EXISTS " + AppDBContract.LinkVideoEntry.TABLE_NAME + "  (" +
                    AppDBContract.LinkVideoEntry._ID + " INTEGER PRIMARY KEY, " +
                    AppDBContract.LinkVideoEntry.COLUMN_NAME_VIDEO_YOUTUBE_ID + " TEXT, " +
                    AppDBContract.LinkVideoEntry.COLUMN_NAME_TITLE + " TEXT, " +
                    AppDBContract.LinkVideoEntry.COLUMN_NAME_URL + " TEXT, " +
                    AppDBContract.LinkVideoEntry.COLUMN_NAME_DESCRIPTION + " TEXT, " +
                    AppDBContract.LinkVideoEntry.COLUMN_NAME_PLAYLIST_ID + " INTEGER " +
                    "REFERENCES " + AppDBContract.PlayListEntry.TABLE_NAME + "(" +
                    AppDBContract.PlayListEntry._ID + ") ON UPDATE CASCADE ON DELETE CASCADE)";

    /*
        CREATE TABLE IF NOT EXISTS bookmark (
            _id INTEGER PRIMARY KEY,
            video_id INTEGER REFERENCES link_video(_id) ON UPDATE CASCADE ON DELETE CASCADE,
            location INTEGER,
            description TEXT)
    */
    private static final String SQL_CREATE_BOOKMARK_ENTRY =
            "CREATE TABLE IF NOT EXISTS " + AppDBContract.BookMarkEntry.TABLE_NAME + "  (" +
                    AppDBContract.BookMarkEntry._ID + " INTEGER PRIMARY KEY, " +
                    AppDBContract.BookMarkEntry.COLUMN_NAME_VIDEO_ID + " INTEGER " +
                    "REFERENCES " + AppDBContract.LinkVideoEntry.TABLE_NAME + "(" +
                    AppDBContract.LinkVideoEntry._ID + ") ON UPDATE CASCADE ON DELETE CASCADE, " +
                    AppDBContract.BookMarkEntry.COLUMN_NAME_LOCATION + " INTEGER, " +
                    AppDBContract.BookMarkEntry.COLUMN_NAME_DESCRIPTION + " TEXT)";

    /*
        CREATE TABLE IF NOT EXISTS link_video_to_playlist (
            playlist_id INTEGER REFERENCES playlist(_id) ON UPDATE CASCADE ON DELETE CASCADE,
            video_id INTEGER REFERENCES link_video(_id) ON UPDATE CASCADE ON DELETE CASCADE)
    */
//    private static final String SQL_CREATE_LINK_VIDEO_TO_PLAYLIST_ENTRY =
//            "CREATE TABLE IF NOT EXISTS " + AppDBContract.LinkVideoToPlayListEntry.TABLE_NAME + "  (" +
//                    AppDBContract.LinkVideoToPlayListEntry.COLUMN_NAME_PLAYLIST_ID + " INTEGER " +
//                    "REFERENCES " + AppDBContract.PlayListEntry.TABLE_NAME + "(" +
//                    AppDBContract.PlayListEntry._ID + ") ON UPDATE CASCADE ON DELETE CASCADE, " +
//                    AppDBContract.LinkVideoToPlayListEntry.COLUMN_NAME_VIDEO_ID + " INTEGER " +
//                    "REFERENCES " + AppDBContract.LinkVideoEntry.TABLE_NAME +"(" +
//                    AppDBContract.LinkVideoEntry._ID + ") ON UPDATE CASCADE ON DELETE CASCADE)";

    public AppDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Timber.d("onCreate 메소드 호출됨");
        try {
            db.execSQL(SQL_CREATE_PLAYLIST_ENTRY);
            db.execSQL(SQL_CREATE_LINK_VIDEO_ENTRY);
            db.execSQL(SQL_CREATE_BOOKMARK_ENTRY);
//            db.execSQL(SQL_CREATE_LINK_VIDEO_TO_PLAYLIST_ENTRY);

            // 기본 재생목록 생
            String sql = "INSERT INTO playlist(playlist_name) VALUES(?)";
            db.execSQL(sql, new Object[]{"기본 재생목록"});
            Timber.d("기본 재생목록 생성됨");
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.d("onUpgrade 메소드 호출됨");
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        Timber.d("onConfigure 메소드 호출됨");
        super.onConfigure(db);
        if (!db.isReadOnly()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                String query = "PRAGMA foreign_keys = ON";
                db.execSQL(query);
            } else {
                db.setForeignKeyConstraintsEnabled(true);
            }
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Timber.d("onOpen 메소드 호출됨");
        super.onOpen(db);

//        try {
//            db.execSQL("drop table link_video_to_playlist");
//            db.execSQL("drop table bookmark");
//            db.execSQL("drop table link_video");
//            db.execSQL("drop table playlist");
//            Timber.d("try 문 안쪽");
//        } catch (Exception e) {
//            Timber.e(e);
//        }
//
//        try {
//            db.execSQL(SQL_CREATE_PLAYLIST_ENTRY);
//            db.execSQL(SQL_CREATE_LINK_VIDEO_ENTRY);
//            db.execSQL(SQL_CREATE_BOOKMARK_ENTRY);
//        } catch (Exception e) {
//            Timber.d(e);
//        }
//
//        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
//
//        if (c.moveToFirst()) {
//            while ( !c.isAfterLast() ) {
//                Timber.d("테이블 " +c.getString(0));
//                c.moveToNext();
//            }
//        }
//        c.close();
    }
}
