#!/bin/bash
# updates the port numbers in argument files such that they don't overlap 

# options
opt_dry=0
#opt_client=0
#opt_server=0

if [[ $# -lt 1 ]]; then
    echo "Usage change_ports.sh [options] folder+"
    exit
fi

match_strings=('\-port' '\-connect' '\-resetPort')
file_increase=0
match_increase=1
folder_step=100
base_port=10000

while [[ "$1" =~ ^- ]]; do case $1 in
        -d | --dry )
            opt_dry=1
            ;;
        -bp | --base-port )
            shift
            base_port=$1
            ;;
        #-c | --client )
        #    match_strings+=('\-port')
        #    opt_client=1
        #    ;;
        #-s | --server )
        #    match_strings+=('\-connect')
        #    opt_server=1
        #    ;;
        -ms | --match_string )
            shift
            match_strings+=("$1")
            ;;
        -fi | --file-increase )
            shift
            file_increase=$1
            ;;
        -fs | --folder-step )
            shift
            file_increase=$1
            ;;
        -mi | --match-increase )
            shift
            match_increase=$1
            ;;
        * )
          echo "Unsupported option $1"
          exit
          ;;
      esac; shift; done

echo "Using port matching strings: " "${match_strings[@]}"

for ((i = 1 ; i <= $# ; i++)); do
    folder="${!i}"
    echo "Folder: $folder"
    for args_file in "$folder"/learn*; do
        echo "File: $args_file"
        for match_string in "${match_strings[@]}"; do
            port=$(grep -A1 "$match_string" "$args_file" | grep -o "[0-9][0-9][0-9][0-9]*" | head -n 1)
            #echo "Matching string: $match_string"
            if [[ -n $port ]]; then
                echo "Port: $port; Matching string: $match_string;"
                num_occur=$(grep --count "$port" "$args_file")
                if [[ ! $num_occur -eq 2 ]]; then
                    echo "Expecting two occurrences, found $num_occur. Skipping"
                else
                    echo "Changing port from $port to $base_port"
                    base_port=$((base_port+match_increase))
                    cmd="sed -i 's/$port/$base_port/g' $args_file"
                    echo "$cmd"
                    if [[ ! $opt_dry -eq 1 ]]; then
                        eval "$cmd"
                    fi
                fi
            fi
        done
        base_port=$((base_port+file_increase))
    done
    if [[ ! $folder_step -eq 0 ]]; then
        base_port=$(((base_port/folder_step)*folder_step+folder_step))
    fi
done
