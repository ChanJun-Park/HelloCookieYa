package edu.inha.hellocookieya.video;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoItem implements Parcelable {
    private int _id;
    private String video_youtube_id;
    private String title;
    private String url;
    private String description;
    private int playlistId;

    public VideoItem() {
    }

    public VideoItem(int _id, String video_youtube_id, String title, String url, String description, int playlistId) {
        this._id = _id;
        this.video_youtube_id = video_youtube_id;
        this.title = title;
        this.url = url;
        this.description = description;
        this.playlistId = playlistId;
    }

    protected VideoItem(Parcel in) {
        _id = in.readInt();
        video_youtube_id = in.readString();
        title = in.readString();
        url = in.readString();
        description = in.readString();
        playlistId = in.readInt();
    }

    public static final Creator<VideoItem> CREATOR = new Creator<VideoItem>() {
        @Override
        public VideoItem createFromParcel(Parcel in) {
            return new VideoItem(in);
        }

        @Override
        public VideoItem[] newArray(int size) {
            return new VideoItem[size];
        }
    };

    public VideoItem set_id(int _id) {
        this._id = _id;
        return this;
    }

    public VideoItem setVideo_youtube_id(String video_youtube_id) {
        this.video_youtube_id = video_youtube_id;
        return this;
    }

    public VideoItem setTitle(String title) {
        this.title = title;
        return this;
    }

    public VideoItem setUrl(String url) {
        this.url = url;
        return this;
    }

    public VideoItem setDescription(String description) {
        this.description = description;
        return this;
    }

    public VideoItem setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
        return this;
    }

    public int get_id() {
        return _id;
    }

    public String getVideo_youtube_id() {
        return video_youtube_id;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public int getPlaylistId() {
        return playlistId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(video_youtube_id);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(description);
        dest.writeInt(playlistId);
    }
}