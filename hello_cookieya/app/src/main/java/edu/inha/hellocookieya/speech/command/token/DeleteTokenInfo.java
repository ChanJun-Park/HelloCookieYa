package edu.inha.hellocookieya.speech.command.token;

import java.util.ArrayList;

import timber.log.Timber;

public class DeleteTokenInfo extends TokenInfo {
    private static ArrayList<String> lexemeList;
    private static DeleteTokenInfo instance;

    private DeleteTokenInfo() {
        lexemeList = new ArrayList<>();
        lexemeList.add("삭제");
        lexemeList.add("제거");
        lexemeList.add("지우기");
    }

    public static DeleteTokenInfo getInstance() {
        if (instance == null) instance = new DeleteTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        return new Token(TOKEN_DELETE);
    }

    @Override
    public boolean check(String str) {
        if (lexemeList != null) {
            for (String lexeme : lexemeList) {
                if (str.contains(lexeme)) return true;
            }
        } else {
            Timber.e("DeleteTokenInfo 의 lexemeList 가 비었음");
        }
        return false;
    }
}
