package edu.inha.hellocookieya.speech.command.token;

public abstract class TokenInfo {
    public static final int TOKEN_PLAY = 1;
    public static final int TOKEN_PAUSE = 1 << 1;
    public static final int TOKEN_SEEK = 1 << 2;
    public static final int TOKEN_CREATE = 1 << 3;
    public static final int TOKEN_BOOKMARK = 1 << 4;
    public static final int TOKEN_VIDEO = 1 << 5;
    public static final int TOKEN_NEXT = 1 << 6;
    public static final int TOKEN_PREVIOUS = 1 << 7;
    public static final int TOKEN_LIST = 1 << 8;
    public static final int TOKEN_QUIT = 1 << 9;
    public static final int TOKEN_NUMBER = 1 << 10;
    public static final int TOKEN_TIME = 1 << 11;
    public static final int TOKEN_FORWARD = 1 << 12;
    public static final int TOKEN_BACKWARD = 1 << 13;
    public static final int TOKEN_SCROLL = 1 << 14;
    public static final int TOKEN_AUTO = 1 << 15;
    public static final int TOKEN_DELETE = 1 << 16;
    public static final int TOKEN_LANDSCAPE = 1 << 17;
    public static final int TOKEN_PORTRAIT = 1 << 18;
    public static final int TOKEN_SCREEN = 1 << 19;

    public abstract Token getToken(String str);
    public abstract boolean check(String str);
}
