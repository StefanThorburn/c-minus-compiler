CIS4650 W21 Compiler's project
Prabhleen Ratra & Stefan Thorburn

***** CHECKPOINT ONE *****
Our parser uses the provided sample scanner and parser as a base

*** RUNNING ***
In root directory, type 'make'

After running 'make':
Where input_file is the relative path from the current directory (e.g. "tests/3.cm")

Run scanner only in root directory with
    'java -cp ./lib/cup.jar:./bin/ Scanner < input_file'
    or
    'bash ./cm.sh input_file -scanner'    

Run parser in root directory with
    'java -cp ./lib/cup.jar:./bin/ Main input_file'
    or
    'bash ./cm.sh input_file'

Type make clean to remove generated files.

Note that all commands are configured for the school linux server and some won't work on e.g. windows powershell