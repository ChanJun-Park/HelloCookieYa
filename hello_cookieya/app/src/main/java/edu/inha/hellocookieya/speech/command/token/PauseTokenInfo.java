package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class PauseTokenInfo extends TokenInfo {

    private static ArrayList<String> lexemeList;

    private static PauseTokenInfo instance;

    private PauseTokenInfo() {
        //TODO : DB와 연동해서 lexemeList 초기화하기
        lexemeList = new ArrayList<String>();
        lexemeList.add("정지");
        lexemeList.add("중지");
        lexemeList.add("일시정지");
        lexemeList.add("잠깐만");
        lexemeList.add("스톱");
        lexemeList.add("멈춰");
    }

    public static PauseTokenInfo getInstance() {
        if (instance == null) instance = new PauseTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_PAUSE);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("PauseTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
