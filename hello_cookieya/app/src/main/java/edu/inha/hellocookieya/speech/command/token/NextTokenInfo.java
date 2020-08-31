package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class NextTokenInfo extends TokenInfo {

    private static ArrayList<String> lexemeList;

    private static NextTokenInfo instance;

    private NextTokenInfo() {
        //TODO : DB와 연동해서 lexemeList 초기화하기
        lexemeList = new ArrayList<String>();
        lexemeList.add("다음");
    }

    public static NextTokenInfo getInstance() {
        if (instance == null) instance = new NextTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_NEXT);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("NextTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
