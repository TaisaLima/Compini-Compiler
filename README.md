# Compini Compiler

Compilador para a linguagem Compini com suporte a paralelismo e canais de comunicação.

## Estrutura do Projeto

```
src/main/java/compiladores/compini/
├── lexer/          # Análise léxica
├── parser/         # Análise sintática
├── ast/            # Árvore sintática abstrata
├── semantic/       # Análise semântica
├── codegen/        # Geração de código
├── runtime/        # Runtime e servidor
└── utils/          # Utilitários
```

## Compilar

```bash
mvn clean compile
```

## Testar

```bash
mvn test
```

## Executar

```bash
mvn package
java -jar target/compini-compiler-1.0-SNAPSHOT.jar input.compini
```
