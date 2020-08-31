package edu.inha.hellocookieya.playlist;

import android.os.Parcel;
import android.os.Parcelable;

public class PlaylistItem implements Parcelable {
    private int _id;
    private String playlistName;
    private int playlistMenuItemId;

    public PlaylistItem() {
    }

    public PlaylistItem(int _id, String playlistName) {
        this._id = _id;
        this.playlistName = playlistName;
    }

    protected PlaylistItem(Parcel in) {
        _id = in.readInt();
        playlistName = in.readString();
        playlistMenuItemId = in.readInt();
    }

    public static final Creator<PlaylistItem> CREATOR = new Creator<PlaylistItem>() {
        @Override
        public PlaylistItem createFromParcel(Parcel in) {
            return new PlaylistItem(in);
        }

        @Override
        public PlaylistItem[] newArray(int size) {
            return new PlaylistItem[size];
        }
    };

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public int getPlaylistMenuItemId() {
        return playlistMenuItemId;
    }

    public void setPlaylistMenuItemId(int playlistMenuItemId) {
        this.playlistMenuItemId = playlistMenuItemId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(playlistName);
        dest.writeInt(playlistMenuItemId);
    }
}
