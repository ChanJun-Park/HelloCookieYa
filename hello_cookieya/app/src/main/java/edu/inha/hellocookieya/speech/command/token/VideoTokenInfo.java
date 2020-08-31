package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class VideoTokenInfo extends TokenInfo {

    private static ArrayList<String> lexemeList;

    private static VideoTokenInfo instance;

    private VideoTokenInfo() {
        //TODO : DB와 연동해서 lexemeList 초기화하기
        lexemeList = new ArrayList<String>();
        lexemeList.add("영상");
        lexemeList.add("비디오");
    }

    public static VideoTokenInfo getInstance() {
        if (instance == null) instance = new VideoTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_VIDEO);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("VideoTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
