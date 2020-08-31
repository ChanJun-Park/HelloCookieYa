package edu.inha.hellocookieya.speech.command.token;

public class Token {
    public static final int N_A = -1;

    private int type = N_A;
    private int value = N_A;

    public Token() {}

    public Token(int type) {
        this.type = type;
    }

    public Token(int type, int value) {
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
