package main.lexer;

public class Word extends Token{
    public String lexema="";
    public Word(String s, int tag){
        super(tag);
        lexema=s;
    }
    @Override
    public String toString() {
        return lexema;
    }
    public static final Word
            and = new Word( "&&", Tag.AND ), or = new Word( "||", Tag.OR ),
            eq = new Word( "==", Tag.EQ ), ne = new Word( "!=", Tag.NE ),
            le = new Word( "<=", Tag.LE ), ge = new Word( ">=", Tag.GE ),
            minus = new Word( "minus", Tag.MINUS ),
            True = new Word( "true", Tag.TRUE ),
            False = new Word( "false", Tag.FALSE ),
            temp = new Word( "t", Tag.TEMP ),
            SEQ = new Word("SEQ", Tag.SEQ),
            PAR = new Word("PAR", Tag.PAR),
            SEND = new Word("send", Tag.SEND),
            RECEIVE = new Word("receive", Tag.RECEIVE),
            INPUT = new Word("input", Tag.INPUT),
            OUTPUT = new Word("output", Tag.OUTPUT),
            C_CHANNEL = new Word("cchannel", Tag.CCHANNEL);
}
