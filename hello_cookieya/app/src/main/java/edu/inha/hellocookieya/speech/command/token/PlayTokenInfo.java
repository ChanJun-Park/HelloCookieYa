package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class PlayTokenInfo extends TokenInfo {

    private static ArrayList<String> lexemeList;

    private static PlayTokenInfo instance;

    private PlayTokenInfo() {
        //TODO : DB와 연동해서 lexemeList 초기화하기
        lexemeList = new ArrayList<>();
        lexemeList.add("재생");
        lexemeList.add("플레이");
        lexemeList.add("시작");
        lexemeList.add("틀어줘");
        lexemeList.add("틀어 줘");
        lexemeList.add("보여줘");
        lexemeList.add("보여 줘");
    }

    public static PlayTokenInfo getInstance() {
        if (instance == null) instance = new PlayTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_PLAY);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("PlayTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
