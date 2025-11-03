package lexer;

import java.io.*;
import java.util.*;
import symbol.*;

public class Lexer {
    public static int line = 1;
    private char peek = ' ';
    private BufferedReader reader;
    private Hashtable<String, Word> words = new Hashtable<>();

    void reserve(Word w) {
        words.put(w.lexema, w);
    }

    public Lexer() {
        reserve(new Word("if", Tag.IF));
        reserve(new Word("else", Tag.ELSE));
        reserve(new Word("while", Tag.WHILE));
        reserve(new Word("do", Tag.DO));
        reserve(new Word("break", Tag.BREAK));
        reserve(new Word("print", Tag.PRINT)); 
        reserve(new Word("seq", Tag.SEQ));    
        reserve(new Word("par", Tag.PAR));     
        reserve(new Word("send", Tag.SEND));  
        reserve(new Word("receive", Tag.RECEIVE));
        reserve(new Word("c_channel", Tag.C_CHANNEL));
        reserve(new Word("input", Tag.INPUT)); 
        reserve(new Word("output", Tag.OUTPUT)); 
        reserve(new Word("string", Tag.STRING));
        reserve(new Word("function", Tag.FUNCTION));
        reserve(new Word("return", Tag.RETURN)); 

        reserve(Word.True);
        reserve(Word.False);
        reserve(Type.Int);
        reserve(Type.Char);
        reserve(Type.Bool);
        reserve(Type.Float);
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public Lexer(FileReader fr) {
        this();
        reader = new BufferedReader(fr);
    }

    public void loadFile(String path) throws FileNotFoundException {
        reader = new BufferedReader(new FileReader(path));
    }

    void readch() throws IOException {
        int r = reader.read();
        peek = (r != -1) ? (char) r : '\u0000';
    }

    boolean readch(char c) throws IOException {
        readch();
        if (peek != c) return false;
        peek = ' ';
        return true;
    }

    public Token nextToken() throws IOException {
        for (; ; readch()) {
            if (peek == ' ' || peek == '\t' || peek == '\r') continue;
            else if (peek == '\n') line++;
            else if (peek == '\u0000') return null;
            else break;
        }

        switch (peek) {
            case '&':
                if (readch('&')) return Word.and;
                else return new Token('&');
            case '|':
                if (readch('|')) return Word.or;
                else return new Token('|');
            case '=':
                if (readch('=')) return Word.eq;
                else return new Token('=');
            case '!':
                if (readch('=')) return Word.ne;
                else return new Token('!');
            case '<':
                if (readch('=')) return Word.le;
                else return new Token('<');
            case '>':
                if (readch('=')) return Word.ge;
                else return new Token('>');
        }

        if (Character.isDigit(peek)) {
            int v = 0;
            do {
                v = 10 * v + Character.digit(peek, 10);
                readch();
            } while (Character.isDigit(peek));
            return new Num(v);
        }

        if (Character.isLetter(peek)) {
            StringBuilder b = new StringBuilder();
            do {
                b.append(peek);
                readch();
            } while (Character.isLetterOrDigit(peek));
            String s = b.toString();
            Word w = words.get(s);
            if (w != null) return w;
            w = new Word(s, Tag.ID);
            words.put(s, w);
            return w;
        }

        Token tok = new Token(peek);
        peek = ' ';
        return tok;
    }
}
