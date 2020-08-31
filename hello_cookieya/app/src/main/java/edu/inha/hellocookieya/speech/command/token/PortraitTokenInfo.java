package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class PortraitTokenInfo extends TokenInfo {
    private static ArrayList<String> lexemeList;

    private static PortraitTokenInfo instance;

    private PortraitTokenInfo() {
        lexemeList = new ArrayList<>();
        lexemeList.add("세로");
        lexemeList.add("세로화면");
        lexemeList.add("작은");
        lexemeList.add("작은화면");
    }

    public static PortraitTokenInfo getInstance() {
        if (instance == null) instance = new PortraitTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_PORTRAIT);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("PortraitTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
