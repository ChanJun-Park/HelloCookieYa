package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class QuitTokenInfo extends TokenInfo {

    private static ArrayList<String> lexemeList;

    private static QuitTokenInfo instance;

    private QuitTokenInfo() {
        //TODO : DB와 연동해서 lexemeList 초기화하기
        lexemeList = new ArrayList<String>();
        lexemeList.add("종료");
        lexemeList.add("끝내기");
    }

    public static QuitTokenInfo getInstance() {
        if (instance == null) instance = new QuitTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_QUIT);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("QuitTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
