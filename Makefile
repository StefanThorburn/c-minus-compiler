SRCDIR=src/
BINDIR=bin/

all: parser

parser:
	$(MAKE) -C $(SRCDIR) Main.class

clean:
	rm -rf $(SRCDIR)Lexer.java $(SRCDIR)parser.java $(SRCDIR)sym.java $(BINDIR)*.class $(BINDIR)absyn $(SRCDIR)*~ ||: