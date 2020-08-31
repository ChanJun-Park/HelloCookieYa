package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class CreateTokenInfo extends TokenInfo {

    private static ArrayList<String> lexemeList;

    private static CreateTokenInfo instance;

    private CreateTokenInfo() {
        //TODO : DB와 연동해서 lexemeList 초기화하기
        lexemeList = new ArrayList<String>();
        lexemeList.add("생성");
        lexemeList.add("추가");
        lexemeList.add("만들기");
    }

    public static CreateTokenInfo getInstance() {
        if (instance == null) instance = new CreateTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_CREATE);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("CreateTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
