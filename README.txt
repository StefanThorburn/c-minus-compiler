CIS4650 W21 Compiler's project
Prabhleen Ratra & Stefan Thorburn

A compiler for the simplified C language known as C- (or C--) -- specification is located in the docs folder.
Developement was divided into 3 phases or checkpoints.

***** CHECKPOINT ONE *****
- Scan input code into tokens
- Parse tokens according to specification grammar into a parse tree
- Perform some syntactic error checking

***** CHECKPOINT TWO *****
- Create a symbol table and perform type checking / semantic error checking.
- Create an annotated parse tree

***** CHECKPOINT THREE *****
- Generate executable assembly code from the annotated parse tree, runnable on the included TMSimulator.

*** RUNNING ***
In root directory, type 'make'

After running 'make':
Where input_file is the relative path from the current directory (e.g. "tests/3.cm")

Run scanner only in root directory with
    'java -cp ./lib/cup.jar:./bin/ Scanner < input_file'

Run parser in root directory with
    'java -cp ./lib/cup.jar:./bin/ Main input_file [-s] [-a] [-c]'
    or
    'bash ./cm.sh input_file [-a] [-s] [-c]'

Type make clean to remove generated files.

Note that all commands are configured for the school linux server and some won't work on e.g. windows powershell.
Additionally, the included cup.jar file is a shortcut to CUP installed on the school server and will not work outside that environment.

*** KNOWN ISSUES ***
- No assembly code is generated for array functionality (memory is allocated correctly, but storing/retrieving values from arrays were not implemented due to time constraints)