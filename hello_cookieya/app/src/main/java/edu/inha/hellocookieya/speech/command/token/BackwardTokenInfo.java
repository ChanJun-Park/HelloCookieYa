package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class BackwardTokenInfo extends TokenInfo {
    private static ArrayList<String> lexemeList;

    private static BackwardTokenInfo instance;

    private BackwardTokenInfo() {
        //TODO : DB와 연동해서 lexemeList 초기화하기
        lexemeList = new ArrayList<>();
        lexemeList.add("뒤로");
        lexemeList.add("전으로");
    }

    public static BackwardTokenInfo getInstance() {
        if (instance == null) instance = new BackwardTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_BACKWARD);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("BackwardTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
