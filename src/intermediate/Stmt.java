package intermediate;

//Construtor de Comando
public class Stmt extends Node{
    public Stmt(){}
    public static Stmt Null = new Stmt();
    public void gen(int a, int b){}
    int after=0;
    
    //Usado pra comando break
    public static Stmt Enclosing=Stmt.Null;
}
