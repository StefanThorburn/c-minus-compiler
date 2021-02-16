JAVAC=javac
JFLEX=jflex
#JFLEX=/Users/fsong/projects/jflex/bin/jflex

all: Token.class Lexer.class Scanner.class

%.class: %.java
	$(JAVAC) $^

Lexer.java: tiny.flex
	$(JFLEX) tiny.flex

clean:
#	Remove in linux terminal
	@rm -f Lexer.java *.class *~ ||:
#	Remove in Windows powershell
	@del -force Lexer.java *.class *~ ||:
#	Remove in windows cmd
	@del /f Lexer.java *.class *~ ||: