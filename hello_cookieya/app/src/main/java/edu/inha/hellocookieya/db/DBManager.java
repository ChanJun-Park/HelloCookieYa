package edu.inha.hellocookieya.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import edu.inha.hellocookieya.playlist.PlaylistItem;
import edu.inha.hellocookieya.video.PlayVideoContent;
import edu.inha.hellocookieya.video.VideoItem;

import java.util.ArrayList;

import timber.log.Timber;

public class DBManager {

    private static DBManager instance;

    private AppDBHelper appDBHelper;
    private SQLiteDatabase database;

    private DBManager(Context context) {
        appDBHelper = getAppDBHelper(context);
        database = appDBHelper.getWritableDatabase();
        if (database != null) {
            Timber.d("새로운 database 생성됨");
        }
    }

    public static void initialize(Context context) {
        Timber.d("initialize 호출됨");
        if (instance == null) {
            Timber.d("새로운 DBManager 생성됨");
            instance = new DBManager(context);
        }
    }

    public static DBManager getInstance(Context context) {
        if (instance == null) {
            instance = new DBManager(context);
        }
        return instance;
    }

    public AppDBHelper getAppDBHelper(Context context) {
        if (appDBHelper == null) {
            appDBHelper = new AppDBHelper(context.getApplicationContext());
            Timber.d("새로운 AppDBHelper 생성됨");
        }
        return appDBHelper;
    }

    public SQLiteDatabase getDatabase(Context context) {
        if (database == null) {
            database = getAppDBHelper(context).getWritableDatabase();
        }
        return database;
    }

    public int addVideoLink(String video_youtube_id, String title, String url, String description, int playlistId) {
        String sql = "insert into link_video(video_youtube_id ,title, url, description, playlist_id) values(?, ?, ?, ?, ?)";
        Object[] params = {video_youtube_id, title, url, description, playlistId};

        int id = -1;

        try {
            database.execSQL(sql, params);
            String getIdSql = "select _id from link_video " +
                    "order by _id desc limit 1";

            Cursor cursor = database.rawQuery(getIdSql, null);
            cursor.moveToNext();
            id = cursor.getInt(0);

            cursor.close();
        } catch (Exception e) {
            Timber.d(e);
        }

        return id;
    }

    public void deleteVideoLink(int _id) {
        String sql = "delete from link_video where _id = ?";

        try {
            database.execSQL(sql, new Object[] {_id});
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public ArrayList<VideoItem> getVideoList(int playlistId) {
        ArrayList<VideoItem> items = null;
        String sql = "SELECT * FROM link_video WHERE playlist_id = " + playlistId;

        if (database != null) {
            items = new ArrayList<VideoItem>();

            Cursor cursor = database.rawQuery(sql, null);
            while(cursor.moveToNext()) {
                VideoItem item = new VideoItem()
                        .set_id(cursor.getInt(0))
                        .setVideo_youtube_id(cursor.getString(1))
                        .setTitle(cursor.getString(2))
                        .setUrl(cursor.getString(3))
                        .setDescription(cursor.getString(4))
                        .setPlaylistId(cursor.getInt(5));


                items.add(item);
            }
            Timber.d("총 %s 개의 영상이 검색됨", items.size());
            cursor.close();
        } else {
            Timber.d("database 객체가 null 포인터임");
        }

        return items;
    }

    public int addBookmark(int videoId, int location, String description) {
        String sql = "insert into bookmark(video_id, location, description) values(?, ?, ?)";
        Object[] params = {videoId, location, description};

        int id = -1;

        try {
            database.execSQL(sql, params);

            String getIdSql = "select _id from bookmark " +
                    "order by _id desc limit 1";

            Cursor cursor = database.rawQuery(getIdSql, null);
            cursor.moveToNext();
            id = cursor.getInt(0);
            cursor.close();

        } catch (Exception e) {
            Timber.d(e);
        }

        return id;
    }

    public ArrayList<PlayVideoContent> getBookmarkList(int video_id) {
        ArrayList<PlayVideoContent> items = null;
        String sql = "select * from bookmark where video_id = " + video_id;

        try {
            items = new ArrayList<PlayVideoContent>();
            Cursor cursor = database.rawQuery(sql, null);
            while(cursor.moveToNext()) {
                PlayVideoContent item = new PlayVideoContent(PlayVideoContent.TYPE_BOOKMARK)
                                            .setBookmarkNumber(cursor.getInt(0))
                                            .setBookmarkTime(cursor.getInt(2))
                                            .setBookmarkDescription(cursor.getString(3));
                items.add(item);
            }

            cursor.close();
        } catch (Exception e) {
            Timber.e(e);
        }

        return items;
    }

    public void editBookmarkTitle(int _id, String title) {
        String sql = "UPDATE bookmark SET description = ? where _id = ?";

        try {
            database.execSQL(sql, new Object[] {title, _id});
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public void deleteBookmark(int _id) {
        String sql = "delete from bookmark where _id = ?";

        try {
            database.execSQL(sql, new Object[] {_id});
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public ArrayList<PlaylistItem> getPlaylistList() {
        ArrayList<PlaylistItem> items = null;
        String sql = "SELECT * FROM playlist";

        try {
            items = new ArrayList<>();
            Cursor cursor = database.rawQuery(sql, null);
            while(cursor.moveToNext()) {
                PlaylistItem item = new PlaylistItem(cursor.getInt(0), cursor.getString(1));
                items.add(item);
            }

            cursor.close();
        } catch (Exception e) {
            Timber.e(e);
        }

        return items;
    }

    public int addPlaylist(String playlistName) {
        String sql = "INSERT INTO playlist(playlist_name) VALUES(?)";
        int id = -1;
        try {
            database.execSQL(sql, new Object[]{ playlistName });

            String getIdSql = "select _id from playlist " +
                    "order by _id desc limit 1";

            Cursor cursor = database.rawQuery(getIdSql, null);
            cursor.moveToNext();
            id = cursor.getInt(0);
            cursor.close();
        } catch (Exception e) {
            Timber.e(e);
        }

        return id;
    }

    public void editPlaylist(int _id, String newName) {
        String sql = "UPDATE playlist SET playlist_name = ? where _id = ?";

        try {
            database.execSQL(sql, new Object[] {newName, _id});
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public void deletePlaylist(int _id) {
        String sql = "delete from playlist where _id = ?";

        try {
            database.execSQL(sql, new Object[] {_id});
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public static void close() {
        if (instance.database != null)
            instance.database.close();
        if (instance.appDBHelper != null)
            instance.appDBHelper.close();

        instance.database = null;
        instance.appDBHelper = null;
        instance = null;
    }
}
