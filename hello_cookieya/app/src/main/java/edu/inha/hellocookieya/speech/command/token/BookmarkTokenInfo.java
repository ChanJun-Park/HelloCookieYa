package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class BookmarkTokenInfo extends TokenInfo {

    private static ArrayList<String> lexemeList;

    private static BookmarkTokenInfo instance;

    private BookmarkTokenInfo() {
        //TODO : DB와 연동해서 lexemeList 초기화하기
        lexemeList = new ArrayList<String>();
        lexemeList.add("북마크");
    }

    public static BookmarkTokenInfo getInstance() {
        if (instance == null) instance = new BookmarkTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_BOOKMARK);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("BookmarkTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
