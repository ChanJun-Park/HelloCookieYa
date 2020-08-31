package edu.inha.hellocookieya.speech.command.token;

import timber.log.Timber;

public class NumberTokenInfo extends TokenInfo {

    private static NumberTokenInfo instance;

    private NumberTokenInfo() {

    }

    public static NumberTokenInfo getInstance() {
        if (instance == null) instance = new NumberTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        int value = -1;
        if (str.contains("번")) {
            value = parseNumberTokenValue(str, "번");
        }
        return new Token(TOKEN_NUMBER, value);
    }

    @Override
    public boolean check(String str) {
        return str.contains("번");
    }

    private int parseNumberTokenValue(String str, String endIndexMarker) {
        int value = -1;
        int startIndex = 0;
        while(startIndex < str.length()) {
            if (str.charAt(startIndex) <= '9' && str.charAt(startIndex) >= '0') {
                break;
            }
            startIndex++;
        }
        int endIndex = str.indexOf(endIndexMarker);
        try {
            value = Integer.parseInt(str.substring(startIndex, endIndex));
        } catch (Exception e) {
            Timber.e(e);
        }

        return value;
    }
}
