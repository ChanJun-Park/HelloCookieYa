package edu.inha.hellocookieya.db;

import android.provider.BaseColumns;

// final class : 상속이 불가능
final class AppDBContract {

    // private 생성자 : 클래스 인스턴스화 불가능
    private AppDBContract() {
    }

    public static class PlayListEntry implements BaseColumns {
        public static final String TABLE_NAME = "playlist";
        public static final String COLUMN_NAME_PLAYLIST_NAME = "playlist_name";
    }

    public static class LinkVideoEntry implements BaseColumns {
        public static final String TABLE_NAME = "link_video";
        public static final String COLUMN_NAME_VIDEO_YOUTUBE_ID ="video_youtube_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_PLAYLIST_ID = "playlist_id";
    }

    public static class BookMarkEntry implements BaseColumns {
        public static final String TABLE_NAME = "bookmark";
        public static final String COLUMN_NAME_VIDEO_ID = "video_id";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
    }
}
