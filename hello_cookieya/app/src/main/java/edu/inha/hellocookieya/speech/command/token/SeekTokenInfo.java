package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class SeekTokenInfo extends TokenInfo {

    private static ArrayList<String> lexemeList;

    private static SeekTokenInfo instance;

    private SeekTokenInfo() {
        //TODO : DB와 연동해서 lexemeList 초기화하기
        lexemeList = new ArrayList<String>();
        lexemeList.add("이동");
        lexemeList.add("점프");
        lexemeList.add("부터");
    }

    public static SeekTokenInfo getInstance() {
        if (instance == null) instance = new SeekTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_SEEK);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("SeekTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
