#!/bin/bash

base_port=$2
match_string="connect"
if [ ! -z "$3" ]; then
    match_string=$3
fi
for args_file in $1learn*; do
    echo "File $args_file"
    port=$(grep -A1 $match_string $args_file | grep -o [0-9][0-9][0-9][0-9]*)
    echo "Port $port"
    if [ ! -z "$base_port" ]; then
       echo "Changing port from $port to $base_port"
       base_port=$((base_port+1))
       cmd="sed -i 's/$port/$base_port/g' $args_file"
       echo $cmd
       eval $cmd
    fi
done
