#!/usr/bin/env bash

dir1=$1
dir2=$2
num_rounds=$3

if [ $# != 3 ]; then
    echo "Usage: ${0##*/} dir1 dir2 num_rounds"
    echo "Diffs the first num_rounds hypotheses stored in two dirs"
    exit 1
fi

if [[ ! -d $dir1 ]]; then
    echo "$dir1"": No such directory"
    exit 1
fi
if [[ ! -d $dir2 ]]; then
    echo "$dir2"": No such directory"
    exit 1
fi

for ((i = 1 ; i <= num_rounds ; i++)); do
    if ! diff --unified=0 "$dir1"/hyp$i.dot "$dir2"/hyp$i.dot; then
        exit 1
    fi
done
