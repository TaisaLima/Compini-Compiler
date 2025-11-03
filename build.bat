@echo off
REM Compila todo o projeto e coloca os .class em bin

echo Compilando o projeto...
javac -d bin src\main\Main.java src\lexer\*.java src\symbol\*.java src\intermediate\*.java src\parser\*.java src\parser\rules\*.java src\tests\*.java

if %ERRORLEVEL% neq 0 (
    echo Erros de compilacao! Corrija antes de rodar.
    pause
    exit /b
)

echo Compilacao concluida com sucesso!
echo.
echo Rodando o compilador...
java -cp bin main.Main
pause
