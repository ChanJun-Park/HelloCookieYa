package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class ForwardTokenInfo extends TokenInfo {
    private static ArrayList<String> lexemeList;

    private static ForwardTokenInfo instance;

    private ForwardTokenInfo() {
        //TODO : DB와 연동해서 lexemeList 초기화하기
        lexemeList = new ArrayList<>();
        lexemeList.add("앞으로");
        lexemeList.add("이후");
    }

    public static ForwardTokenInfo getInstance() {
        if (instance == null) instance = new ForwardTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_FORWARD);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("ForwardTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
