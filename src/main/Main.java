package main;
import java.io.*;
import lexer.Lexer;
import parser.Parser;



public class Main {

    public static void Menu () {
    System.out.println("---------Bem-vindo ao Compilador da linguagem Compini!--------");
    System.out.println("\n");
    System.out.println("Selecione a sua preferência       1. Testar o compilador manualmente         2. Explorar os testes disponíveis na nossa base de dados ");
    }
    public static void main(String[] args) throws IOException{
        Menu();
    }
}