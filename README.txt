CIS4650 W21 Compiler's project
Prabhleen Ratra & Stefan Thorburn

***** CHECKPOINT ONE *****
Our scanner uses the provided sample scanner and parser as a base


*** RUNNING ***
In root directory, type 'make'

After running 'make':
Run scanner in root directory with
    'java -cp ./lib/cup.jar:./bin/ Scanner < input_file'
    or
    'bash ./cm.sh input_file -scanner'

Run parser in root directory with
    'java -cp ./lib/cup.jar:./bin/ Main input_file'
    or
    'bash ./cm.sh input_file'

Note that it won't work with windows powershell (doesn't allow < operator)

Type 'make clean' to remove compiled files
