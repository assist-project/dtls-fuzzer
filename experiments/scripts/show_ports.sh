#!/bin/bash

match_string="connect"
if [ ! -z "$2" ]; then
    match_string=$2
fi
for args_file in $1/learn*; do
    echo "File $args_file"
    port=$(grep -A1 $match_string $args_file | grep -o [0-9][0-9][0-9]*)
    echo "Port $port"
done
