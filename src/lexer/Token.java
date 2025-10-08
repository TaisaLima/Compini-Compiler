package lexer;

//Classe Abstrata para os diferentes tipos de Token
public class Token {
    public final int tag;
    public Token(int t) {
        tag = t;
    }
    public String toString() {
        return "" + (char)tag;
    }
}
