#!/bin/bash
learning_folder1=$1
learning_folder2=$2
rounds=$3

if [ $# != 3 ]; then
    echo "Diffs the hyps generated in the first [rounds] by two learning experiments"   
    echo "usage: bash script learning_folder1 learning_folder2 rounds"
    exit 1
fi

if [[ ! -d $learning_folder1 || ! -d $learning_folder2 ]]; then
    echo "learning folders cannot be empty (they should contain at least the hyp files)"
    exit 1
fi

for ((i = 1 ; i <= $rounds ; i++)); do
    diff -w $learning_folder1/hyp$i.dot $learning_folder2/hyp$i.dot
done