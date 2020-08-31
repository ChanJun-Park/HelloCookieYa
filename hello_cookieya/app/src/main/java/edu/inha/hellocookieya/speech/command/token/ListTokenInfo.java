package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class ListTokenInfo extends TokenInfo {

    private static ArrayList<String> lexemeList;

    private static ListTokenInfo instance;

    private ListTokenInfo() {
        //TODO : DB와 연동해서 lexemeList 초기화하기
        lexemeList = new ArrayList<String>();
        lexemeList.add("목록");
        lexemeList.add("재생목록");
        lexemeList.add("영상목록");
        lexemeList.add("리스트");
    }

    public static ListTokenInfo getInstance() {
        if (instance == null) instance = new ListTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_LIST);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("ListTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
