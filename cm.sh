#!/bin/sh

if (( $# < 1 )); then
    >&2 echo "Usage: bash ./cm.sh input_file [-a] [-s] [-c]"
else
   echo -n 'Running C- parser with args: '
   echo "$@"
   java -cp ./lib/cup.jar:./bin/ CM "$@"   
fi