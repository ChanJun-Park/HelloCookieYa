package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class PreviousTokenInfo extends TokenInfo {

    private static ArrayList<String> lexemeList;

    private static PreviousTokenInfo instance;

    private PreviousTokenInfo() {
        //TODO : DB와 연동해서 lexemeList 초기화하기
        lexemeList = new ArrayList<String>();
        lexemeList.add("이전");
    }

    public static PreviousTokenInfo getInstance() {
        if (instance == null) instance = new PreviousTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_PREVIOUS);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("PreviousTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
