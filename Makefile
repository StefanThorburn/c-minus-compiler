JAVAC=javac
JFLEX=jflex
SRCDIR=src/
BINDIR=bin/
#JFLEX=/Users/fsong/projects/jflex/bin/jflex

all: scanner

scanner: $(BINDIR)Token.class $(BINDIR)Lexer.class $(BINDIR)Scanner.class

$(BINDIR)%.class: $(SRCDIR)%.java
	$(JAVAC) -d $(BINDIR) $^ -sourcepath $(SRCDIR)

$(SRCDIR)Lexer.java: $(SRCDIR)cm.flex
	$(JFLEX) -d $(SRCDIR) $(SRCDIR)cm.flex

# Removes compiled files in either a linux or windows environment
# Includes a version for windows powershell and cmd
# Suppresses error messages from missing commands
clean:
	-rm -f $(SRCDIR)Lexer.java $(BINDIR)*.class $(SRCDIR)*~ ||:
	-rm -force $(SRCDIR)Lexer.java $(BINDIR)*.class $(SRCDIR)*~ ||:
	-del /f src\Lexer.java bin\*.class src\*~ ||: