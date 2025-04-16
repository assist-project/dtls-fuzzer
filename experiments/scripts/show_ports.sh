#!/usr/bin/env bash

if [[ $# -lt 2 ]]; then
    echo "Usage show_ports.sh args_dir [match_strings*]"
    exit
fi

for args_file in "$1"/learn*; do
    echo "File ${args_file}"
    for ((i = 2 ; i <= $# ; i++)); do
        echo "${i}"
        match_string="${!i}"
        port=$(grep -A1 "${match_string}" "${args_file}" | grep -o "[0-9][0-9][0-9]*" | head -n 1)
        echo "Port ${port}"
    done
done
