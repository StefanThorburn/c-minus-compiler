SRCDIR=src/
BINDIR=bin/

all: parser

parser:
	$(MAKE) -C $(SRCDIR) CM.class

clean:
	rm -rf $(SRCDIR)Lexer.java $(SRCDIR)parser.java $(SRCDIR)sym.java $(BINDIR)*.class $(BINDIR)absyn $(SRCDIR)*~ ||: