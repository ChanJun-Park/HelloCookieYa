package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class LandscapeTokenInfo extends TokenInfo {
    private static ArrayList<String> lexemeList;

    private static LandscapeTokenInfo instance;

    private LandscapeTokenInfo() {
        lexemeList = new ArrayList<>();
        lexemeList.add("전체화면");
        lexemeList.add("전체");
        lexemeList.add("가로");
        lexemeList.add("가로화면");
        lexemeList.add("큰");
    }

    public static LandscapeTokenInfo getInstance() {
        if (instance == null) instance = new LandscapeTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_LANDSCAPE);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("LandscapeTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
