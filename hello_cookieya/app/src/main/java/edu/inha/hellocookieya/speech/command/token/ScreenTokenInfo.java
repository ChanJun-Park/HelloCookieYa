package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class ScreenTokenInfo extends TokenInfo {
    private static ArrayList<String> lexemeList;

    private static ScreenTokenInfo instance;

    private ScreenTokenInfo() {
        lexemeList = new ArrayList<>();
        lexemeList.add("화면");
        lexemeList.add("스크린");
    }

    public static ScreenTokenInfo getInstance() {
        if (instance == null) instance = new ScreenTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_SCREEN);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("ScreenTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
