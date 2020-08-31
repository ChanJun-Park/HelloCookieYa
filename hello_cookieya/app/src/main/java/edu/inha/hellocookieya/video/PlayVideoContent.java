package edu.inha.hellocookieya.video;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

public class PlayVideoContent implements Parcelable, Comparable<PlayVideoContent> {
    public static final int TYPE_DESCRIPTION = 0;
    public static final int TYPE_SEPARATOR = 1;
    public static final int TYPE_BOOKMARK = 2;
    public static final int TYPE_FOOTER = 3;

    private int contentType;
    private String title;
    private String description;
    private int bookmarkNumber;
    private int bookmarkTime;
    private String bookmarkDescription;
    private int adapterIndex;

    public PlayVideoContent() {}

    public PlayVideoContent(int contentType) {
        this.contentType = contentType;
    }

    public PlayVideoContent(int contentType, String title, String description) {
        this.contentType = contentType;
        this.title = title;
        this.description = description;
    }

    public PlayVideoContent(int contentType, int bookmarkNumber, int bookmarkTime, String bookmarkDescription) {
        this.contentType = contentType;
        this.bookmarkNumber = bookmarkNumber;
        this.bookmarkTime = bookmarkTime;
        this.bookmarkDescription = bookmarkDescription;
    }

    protected PlayVideoContent(Parcel in) {
        contentType = in.readInt();
        title = in.readString();
        description = in.readString();
        bookmarkNumber = in.readInt();
        bookmarkTime = in.readInt();
        bookmarkDescription = in.readString();
        adapterIndex = in.readInt();
    }

    public static final Creator<PlayVideoContent> CREATOR = new Creator<PlayVideoContent>() {
        @Override
        public PlayVideoContent createFromParcel(Parcel in) {
            return new PlayVideoContent(in);
        }

        @Override
        public PlayVideoContent[] newArray(int size) {
            return new PlayVideoContent[size];
        }
    };

    public int getContentType() {
        return contentType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getBookmarkNumber() {
        return bookmarkNumber;
    }

    public int getBookmarkTime() {
        return bookmarkTime;
    }

    public String getBookmarkDescription() {
        return bookmarkDescription;
    }

    public int getAdapterIndex() {
        return adapterIndex;
    }

    public PlayVideoContent setContentType(int contentType) {
        this.contentType = contentType;
        return this;
    }

    public PlayVideoContent setTitle(String title) {
        this.title = title;
        return this;
    }

    public PlayVideoContent setDescription(String description) {
        this.description = description;
        return this;
    }

    public PlayVideoContent setBookmarkNumber(int bookmarkNumber) {
        this.bookmarkNumber = bookmarkNumber;
        return this;
    }

    public PlayVideoContent setBookmarkTime(int bookmarkTime) {
        this.bookmarkTime = bookmarkTime;
        return this;
    }

    public PlayVideoContent setBookmarkDescription(String bookmarkDescription) {
        this.bookmarkDescription = bookmarkDescription;
        return this;
    }

    public PlayVideoContent setAdapterIndex(int adapterIndex) {
        this.adapterIndex = adapterIndex;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(contentType);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeInt(bookmarkNumber);
        dest.writeInt(bookmarkTime);
        dest.writeString(bookmarkDescription);
        dest.writeInt(adapterIndex);
    }

    @Override
    public int compareTo(@NotNull PlayVideoContent playVideoContent) {
        if (this.contentType == TYPE_BOOKMARK && playVideoContent.contentType == TYPE_BOOKMARK) {
            return this.bookmarkTime - playVideoContent.bookmarkTime;
        } else {
            return this.contentType - playVideoContent.contentType;
        }
    }
}
