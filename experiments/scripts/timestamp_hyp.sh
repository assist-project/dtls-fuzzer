#!/usr/bin/env bash

learning_folder=$1
result_folder=$2
prefix=$3

if [ $# != 3 ]; then
    echo "usage: bash script learning_folder result_folder prefix"
    exit 1
fi

if [[ ! -d $result_folder ]]; then
    mkdir "$result_folder"
fi

start_time=$(stat --format %W "$learning_folder"/alphabet.xml)
hour_index=0

for hyp in "$learning_folder"/hyp*
do
    hyp_gen_time=$(stat --format %W "$hyp")
    hours_passed=$((hyp_gen_time/3600-start_time/3600))
    if (( hours_passed > hour_index )); then
        hour_index=$hours_passed
        ts_hyp_path=$result_folder/$prefix"_$hour_index".dot
        cp "$hyp" "$ts_hyp_path"
    fi
done
