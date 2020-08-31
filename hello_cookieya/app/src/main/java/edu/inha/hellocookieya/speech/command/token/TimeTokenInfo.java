package edu.inha.hellocookieya.speech.command.token;

import timber.log.Timber;

public class TimeTokenInfo extends TokenInfo {

    private static TimeTokenInfo instance;

    private TimeTokenInfo() {

    }

    public static TimeTokenInfo getInstance() {
        if (instance == null) instance = new TimeTokenInfo();
        return instance;
    }

    @Override
    public Token getToken(String str) {
        int value = -1;
        if (str.contains("시간")) {
            value = parseNumberTokenValue(str, "시간") * 3600;
        } else if (str.contains("분")) {
            value = parseNumberTokenValue(str, "분") * 60;
        } else if (str.contains("초")) {
            value = parseNumberTokenValue(str, "초");
        }
        return new Token(TOKEN_TIME, value);
    }

    @Override
    public boolean check(String str) {
        return str.contains("시간") || str.contains("분") || str.contains("초");
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
