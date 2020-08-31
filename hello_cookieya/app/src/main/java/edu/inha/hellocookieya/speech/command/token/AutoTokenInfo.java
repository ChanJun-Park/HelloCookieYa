package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class AutoTokenInfo extends TokenInfo {
    private static ArrayList<String> lexemeList;

    private static AutoTokenInfo instance;

    private AutoTokenInfo() {
        lexemeList = new ArrayList<>();
        lexemeList.add("자동");
    }

    public static AutoTokenInfo getInstance() {
        if (instance == null) instance = new AutoTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_AUTO);
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
