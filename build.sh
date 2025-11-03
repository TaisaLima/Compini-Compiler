
javac -d bin $(find src/main -name "*.java") $(find src/lexer -name "*.java") \
$(find src/symbol -name "*.java") $(find src/intermediate -name "*.java") \
$(find src/parser -name "*.java") $(find src/parser/rules -name "*.java") \
$(find src/tests -name "*.java")

if [ $? -ne 0 ]; then
    echo "erro"
    exit 1
fi

java -cp bin main.Main
