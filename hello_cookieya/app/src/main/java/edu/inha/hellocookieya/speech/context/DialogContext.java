package edu.inha.hellocookieya.speech.context;

import android.os.Parcel;
import android.os.Parcelable;

public class DialogContext implements Parcelable {

    public static final int INPUT_BOOKMARK_NAME = 1;

    private int type;
    private int targetEntityId;

    public DialogContext() {
    }

    protected DialogContext(Parcel in) {
        type = in.readInt();
        targetEntityId = in.readInt();
    }

    public static final Creator<DialogContext> CREATOR = new Creator<DialogContext>() {
        @Override
        public DialogContext createFromParcel(Parcel in) {
            return new DialogContext(in);
        }

        @Override
        public DialogContext[] newArray(int size) {
            return new DialogContext[size];
        }
    };

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTargetEntityId() {
        return targetEntityId;
    }

    public void setTargetEntityId(int targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeInt(targetEntityId);
    }
}
