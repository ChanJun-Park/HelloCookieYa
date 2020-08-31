package edu.inha.hellocookieya.speech.command;

import edu.inha.hellocookieya.speech.command.token.AutoTokenInfo;
import edu.inha.hellocookieya.speech.command.token.BackwardTokenInfo;
import edu.inha.hellocookieya.speech.command.token.BookmarkTokenInfo;
import edu.inha.hellocookieya.speech.command.token.CreateTokenInfo;
import edu.inha.hellocookieya.speech.command.token.DeleteTokenInfo;
import edu.inha.hellocookieya.speech.command.token.ForwardTokenInfo;
import edu.inha.hellocookieya.speech.command.token.LandscapeTokenInfo;
import edu.inha.hellocookieya.speech.command.token.ListTokenInfo;
import edu.inha.hellocookieya.speech.command.token.NextTokenInfo;
import edu.inha.hellocookieya.speech.command.token.NumberTokenInfo;
import edu.inha.hellocookieya.speech.command.token.PauseTokenInfo;
import edu.inha.hellocookieya.speech.command.token.PlayTokenInfo;
import edu.inha.hellocookieya.speech.command.token.PortraitTokenInfo;
import edu.inha.hellocookieya.speech.command.token.PreviousTokenInfo;
import edu.inha.hellocookieya.speech.command.token.QuitTokenInfo;
import edu.inha.hellocookieya.speech.command.token.ScreenTokenInfo;
import edu.inha.hellocookieya.speech.command.token.ScrollTokenInfo;
import edu.inha.hellocookieya.speech.command.token.SeekTokenInfo;
import edu.inha.hellocookieya.speech.command.token.TimeTokenInfo;
import edu.inha.hellocookieya.speech.command.token.Token;
import edu.inha.hellocookieya.speech.command.token.TokenInfo;
import edu.inha.hellocookieya.speech.command.token.VideoTokenInfo;

import java.util.ArrayList;
import java.util.StringTokenizer;

import timber.log.Timber;

public class CommandParser {
    private static ArrayList<TokenInfo> tokenInfoItems;
    private static CommandParser instance;

    private CommandParser() {
        tokenInfoItems = new ArrayList<>();
        tokenInfoItems.add(PlayTokenInfo.getInstance());
        tokenInfoItems.add(PauseTokenInfo.getInstance());
        tokenInfoItems.add(SeekTokenInfo.getInstance());
        tokenInfoItems.add(CreateTokenInfo.getInstance());
        tokenInfoItems.add(BookmarkTokenInfo.getInstance());
        tokenInfoItems.add(VideoTokenInfo.getInstance());
        tokenInfoItems.add(NextTokenInfo.getInstance());
        tokenInfoItems.add(PreviousTokenInfo.getInstance());
        tokenInfoItems.add(ListTokenInfo.getInstance());
        tokenInfoItems.add(QuitTokenInfo.getInstance());
        tokenInfoItems.add(NumberTokenInfo.getInstance());
        tokenInfoItems.add(TimeTokenInfo.getInstance());
        tokenInfoItems.add(ForwardTokenInfo.getInstance());
        tokenInfoItems.add(BackwardTokenInfo.getInstance());
        tokenInfoItems.add(ScrollTokenInfo.getInstance());
        tokenInfoItems.add(AutoTokenInfo.getInstance());
        tokenInfoItems.add(DeleteTokenInfo.getInstance());
        tokenInfoItems.add(LandscapeTokenInfo.getInstance());
        tokenInfoItems.add(PortraitTokenInfo.getInstance());
        tokenInfoItems.add(ScreenTokenInfo.getInstance());
    }

    public static CommandParser getInstance() {
        if (instance == null) instance = new CommandParser();
        return instance;
    }

    private Token parseToken(String strToken) {
        Token resultToken = null;
        for (TokenInfo info : tokenInfoItems) {
            if (info.check(strToken)) {
                resultToken = info.getToken(strToken);
                break;
            }
        }
        return resultToken;
    }

    public Command parseCommand(String str) {
        int command = 0;
        int paramValue = 0;

        StringTokenizer tokenizer = new StringTokenizer(str);
        while(tokenizer.hasMoreTokens()) {
            String strToken = tokenizer.nextToken();
            Timber.d("문자열 토큰 : %s", strToken);
            Token token = parseToken(strToken);
            if (token != null) {
                command |= token.getType();
                Timber.d("토큰 타입 : %s", token.getType());
                if (token.getValue() != Token.N_A) {
                    paramValue += token.getValue();
                }
            } else {
                Timber.d("파싱되지 못한 토큰 : %s", strToken);
            }
        }

        Timber.d("최종 파싱된 command 값 : %s", command);
        return new Command(command, paramValue);
    }

}
