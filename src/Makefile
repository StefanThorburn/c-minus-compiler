JAVA=java
JAVAC=javac
BINDIR=../bin/
JFLEX=jflex
#CLASSPATH=-cp /usr/share/java/cup.jar:.
CLASSPATH=-cp ../lib/cup.jar:.
CUP=cup

all: CM.class

CM.class: absyn/*.java parser.java sym.java Lexer.java CodeGenerator.java SemanticAnalyzer.java NodeType.java ShowTreeVisitor.java Scanner.java CM.java

%.class: %.java
	$(JAVAC) -d $(BINDIR) $(CLASSPATH) $^

Lexer.java: cm.flex
	$(JFLEX) cm.flex

parser.java: cm.cup
	#$(CUP) -dump -expect 3 cm.cup
	$(CUP) -expect 3 cm.cup
