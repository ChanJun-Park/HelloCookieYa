package edu.inha.hellocookieya.speech.command;

import android.os.Parcel;
import android.os.Parcelable;

import edu.inha.hellocookieya.speech.command.token.TokenInfo;

public class Command implements Parcelable {
    private int command = 0;
    private int paramValue = 0;

    public Command() {}

    public Command(int command) {
        this.command = command;
    }

    public Command(int command, int paramValue) {
        this.command = command;
        this.paramValue = paramValue;
    }

    protected Command(Parcel in) {
        command = in.readInt();
        paramValue = in.readInt();
    }

    public static final Creator<Command> CREATOR = new Creator<Command>() {
        @Override
        public Command createFromParcel(Parcel in) {
            return new Command(in);
        }

        @Override
        public Command[] newArray(int size) {
            return new Command[size];
        }
    };

    public int getCommand() {
        return command;
    }

    public int getParamValue() {
        return paramValue;
    }

    public CommandEnum getCmdType() {
        CommandEnum cmdType;

        // 조건 3개
        if (isCommandBitSet(TokenInfo.TOKEN_PLAY) && isCommandBitSet(TokenInfo.TOKEN_VIDEO)
                && isCommandBitSet(TokenInfo.TOKEN_NUMBER)) {
            cmdType = CommandEnum.COMMAND_PLAY_NTH_VIDEO;
        } else if (isCommandBitSet(TokenInfo.TOKEN_DELETE) && isCommandBitSet(TokenInfo.TOKEN_VIDEO)
                && isCommandBitSet(TokenInfo.TOKEN_NUMBER)) {
            cmdType = CommandEnum.COMMAND_DELETE_NTH_VIDEO;
        } else if (isCommandBitSet(TokenInfo.TOKEN_DELETE) && isCommandBitSet(TokenInfo.TOKEN_BOOKMARK)
                && isCommandBitSet(TokenInfo.TOKEN_NUMBER)) {
            cmdType = CommandEnum.COMMAND_DELETE_NTH_BOOKMARK;
        } else if (isCommandBitSet(TokenInfo.TOKEN_SEEK) && isCommandBitSet(TokenInfo.TOKEN_FORWARD)
                && isCommandBitSet(TokenInfo.TOKEN_TIME)) {
            cmdType = CommandEnum.COMMAND_RELATIVE_SEEK;
        } else if (isCommandBitSet(TokenInfo.TOKEN_SEEK) && isCommandBitSet(TokenInfo.TOKEN_BACKWARD)
                && isCommandBitSet(TokenInfo.TOKEN_TIME)) {
            cmdType = CommandEnum.COMMAND_RELATIVE_SEEK;
            paramValue *= -1;
        } else if (isCommandBitSet(TokenInfo.TOKEN_BOOKMARK) && isCommandBitSet(TokenInfo.TOKEN_SEEK)
                && isCommandBitSet(TokenInfo.TOKEN_NUMBER)) {
            cmdType = CommandEnum.COMMAND_SEEK_TO_BOOKMARK;
        } else if (isCommandBitSet(TokenInfo.TOKEN_AUTO) && isCommandBitSet(TokenInfo.TOKEN_SCROLL)
                && isCommandBitSet(TokenInfo.TOKEN_PAUSE)) {
            cmdType = CommandEnum.COMMAND_AUTO_SCROLL_STOP;
        } else if (isCommandBitSet(TokenInfo.TOKEN_LANDSCAPE) && isCommandBitSet(TokenInfo.TOKEN_SCREEN)
                && isCommandBitSet(TokenInfo.TOKEN_QUIT)) {
            cmdType = CommandEnum.COMMAND_PORTRAIT_MODE;
        // 조건 2개
        } else if (isCommandBitSet(TokenInfo.TOKEN_LANDSCAPE) && isCommandBitSet(TokenInfo.TOKEN_SCREEN)) {
            cmdType = CommandEnum.COMMAND_LANDSCAPE_MODE;
        } else if (isCommandBitSet(TokenInfo.TOKEN_PORTRAIT) && isCommandBitSet(TokenInfo.TOKEN_SCREEN)) {
            cmdType = CommandEnum.COMMAND_PORTRAIT_MODE;
        }
        else if (isCommandBitSet(TokenInfo.TOKEN_PLAY) && isCommandBitSet(TokenInfo.TOKEN_NEXT)) {
            cmdType = CommandEnum.COMMAND_PLAY_NEXT_VIDEO;
        } else if (isCommandBitSet(TokenInfo.TOKEN_PLAY) && isCommandBitSet(TokenInfo.TOKEN_PREVIOUS)) {
            cmdType = CommandEnum.COMMAND_PLAY_PREV_VIDEO;
        } else if (isCommandBitSet(TokenInfo.TOKEN_PLAY) && isCommandBitSet(TokenInfo.TOKEN_TIME)) {
            cmdType = CommandEnum.COMMAND_SEEK;
        } else if (isCommandBitSet(TokenInfo.TOKEN_SEEK) && isCommandBitSet(TokenInfo.TOKEN_TIME)) {
            cmdType = CommandEnum.COMMAND_SEEK;
        } else if (isCommandBitSet(TokenInfo.TOKEN_CREATE) && isCommandBitSet(TokenInfo.TOKEN_BOOKMARK)) {
            cmdType = CommandEnum.COMMAND_CREATE_BOOKMARK;
        } else if (isCommandBitSet(TokenInfo.TOKEN_AUTO) && isCommandBitSet(TokenInfo.TOKEN_SCROLL)) {
            cmdType = CommandEnum.COMMAND_AUTO_SCROLL;
        // 조건 1
        } else if (isCommandBitSet(TokenInfo.TOKEN_LANDSCAPE)) {
            cmdType = CommandEnum.COMMAND_LANDSCAPE_MODE;
        } else if (isCommandBitSet(TokenInfo.TOKEN_PORTRAIT)) {
            cmdType = CommandEnum.COMMAND_PORTRAIT_MODE;
        } else if (isCommandBitSet(TokenInfo.TOKEN_PLAY)) {
            cmdType = CommandEnum.COMMAND_PLAY;
        } else if (isCommandBitSet(TokenInfo.TOKEN_PAUSE)) {
            cmdType = CommandEnum.COMMAND_PAUSE;
        } else if (isCommandBitSet(TokenInfo.TOKEN_LIST)) {
            cmdType = CommandEnum.COMMAND_MOVE_TO_LIST;
        } else if (isCommandBitSet(TokenInfo.TOKEN_QUIT)) {
            cmdType = CommandEnum.COMMAND_QUIT;
        } else {
            cmdType = CommandEnum.UNDEFINED_COMMAND;
        }
        return cmdType;
    }

    private boolean isCommandBitSet(int tokenInfoType) {
        return (command & tokenInfoType) != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(command);
        dest.writeInt(paramValue);
    }
}