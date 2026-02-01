JAVA=java
JAVAC=javac
JFLEX=$(JAVA) -jar jflex-full-1.8.2.jar
CUPJAR=./java-cup-11b.jar
CUP=$(JAVA) -jar $(CUPJAR)
CP=.:$(CUPJAR)

default: run

.SUFFIXES: $(SUFFIXES) .class .java

.java.class:
		$(JAVAC) -cp $(CP) $*.java

FILE=    Lexer.java      parser.java    sym.java \
		ArgDecl.java ArgDeclArray.java ArgDecls.java ArgDeclSingle.java \
		ArgFuncStmt.java Args.java AssmntStmt.java TI.java BinaryExpr.java \
		BoolLit.java CharLit.java Expr.java FieldDecl.java \
		FieldDeclArray.java FieldDecls.java FieldDeclSingle.java FloatLit.java \
		FuncStmt.java IfBackend.java IfBackendStmt.java IfBase.java IfStmt.java \
		IntLit.java MemberDecls.java MethodDecl.java MethodDecls.java \
		Name.java NameArray.java NonIfStmt.java OptionalExpr.java \
		OptionalFinal.java OptionalSemi.java ParenExpr.java PrintList.java \
		PrintLnList.java PrintLnStmt.java PrintStmt.java Program.java ReadList.java \
		ReadStmt.java ReturnType.java ScopeStmt.java Stmt.java Stmts.java \
		StrLit.java TernaryExpr.java Type.java TypeCastExpr.java TypeLit.java \
		UnaryExpr.java UnaryStmt.java ValueReturn.java VoidFuncStmt.java VoidReturn.java \
		WhileStmt.java FuncExpr.java VoidFuncExpr.java \
		ArgFuncExpr.java FieldStart.java FieldsNMethods.java VoidType.java \
		WhileBase.java Token.java IntType.java FloatType.java \
		CharType.java BoolType.java NonTypeCastExpr.java ActionExpr.java\
    SymbolTable.java LangException.java Pair.java InOutList.java InOut.java\
		ScopeContext.java TypeCheckingTest.java ScannerTest.java LexerTest.java 

run: test1Output.txt test2Output.txt

all: Lexer.java parser.java $(FILE:java=class)

test1Output.txt: all
	$(JAVA) -cp $(CP) ScannerTest < test1.as > test1Output.txt
	cat -n test1Output.txt

test2Output.txt: all
	$(JAVA) -cp $(CP) ScannerTest < test2.as > test2Output.txt
	cat -n test2Output.txt

clean:
		rm -f *.class *~ *.bak Lexer.java parser.java sym.java

Lexer.java: tokens.jflex
		$(JFLEX) tokens.jflex

parser.java: grammar.cup
		$(CUP) -interface < grammar.cup

parserD.java: grammar.cup
		$(CUP) -interface -dump < grammar.cup

typecheck: all
	@echo "Running type checking tests"
	@for file in p3tests/p3tests/*.as; do \
		basename=$$(basename $$file .as); \
		echo "Testing: $$file -> $$basename-output.txt"; \
		$(JAVA) -cp $(CP) TypeCheckingTest $$file > $$basename-output.txt 2>&1; \
		echo "Output saved to $$basename-output.txt"; \
		echo ""; \
	done
