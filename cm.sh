#!/bin/sh

if (( $# < 1 )); then
    >&2 echo "Usage: bash ./cm.sh input_file [-scanner]"
else
   if [ $# -gt 1 ] && [ $2 == '-scanner' ]
   then
      echo "Running C- scanner with input file $1"      
      java -cp ./lib/cup.jar:./bin/ Scanner < $1
   else
      echo "Running C- parser with input file $1"
      java -cp ./lib/cup.jar:./bin/ Main $1      
   fi
fi