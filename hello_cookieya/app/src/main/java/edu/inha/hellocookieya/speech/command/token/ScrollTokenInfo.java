package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class ScrollTokenInfo extends TokenInfo  {
    private static ArrayList<String> lexemeList;

    private static ScrollTokenInfo instance;

    private ScrollTokenInfo() {
        //TODO : DB와 연동해서 lexemeList 초기화하기
        lexemeList = new ArrayList<>();
        lexemeList.add("스크롤");
    }

    public static ScrollTokenInfo getInstance() {
        if (instance == null) instance = new ScrollTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_SCROLL);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("ScrollTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
