#!/bin/bash
# Script for improving the visuals of a learned model

readonly SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
readonly EXP_SCRIPTS_DIR="$SCRIPT_DIR/experiments/scripts/"
readonly COLOR_FILE="$EXP_SCRIPTS_DIR/coloredPaths.json"
readonly REPL_FILE="$EXP_SCRIPTS_DIR/replacements.json"
readonly FLAG_SET="1"

# transforms capitalized snake case (CLIENT_HELLO) to came case (ClientHello)
function to_camel() {
    sed -i -r 's/([A-Z])([A-Z]*)_*/\1\L\2/g' $1
}

# shortens state labels from (for example) s3 to 3
function shorten_state_labels() {
    sed -i -r 's/\"s([0-9])/\"\1/g' $1
}

function relabel_and_color() {
    echo "Formatting model by relabeling and coloring handshakes"
    extra=''
    if [[ $3 == $FLAG_SET ]] ; then
        echo "State pruning enabled"
        extra='-ego "\s*App"'
    else
        echo "State pruning disabled"
        extra=''
    fi
    #echo java -jar $EXP_SCRIPTS_DIR/dot-trimmer.jar -r $REPL_FILE -i $1 -o $2 -t 3 -pc $COLOR_FILE $extra
    java -jar $EXP_SCRIPTS_DIR/dot-trimmer.jar -r $REPL_FILE -i $1 -o $2 -t 3 -pc $COLOR_FILE $extra
}

function merge_transitions() {
    echo "Formatting model by merging transitions"
    python3 $EXP_SCRIPTS_DIR/dotformat.py $1
}


function trim() {
    model=$1
    smodel=${model%dot}simplified.dot
    cp $model $smodel
    to_camel $smodel
    
    if [ $2 ]; then 
        tmodel=$2
    else
        tmodel=${model%dot}trimmed.dot
    fi
    
    relabel_and_color $smodel $tmodel $3

    if [[ $4 == $FLAG_SET ]] ; then
        merge_transition $tmodel $tmodel
    fi

    shorten_state_labels $tmodel
    rm $smodel 
}

if [ $# = 0 ]; then 
    echo "Usage trim_model.sh dot_model [dot_output [prune_states_flag [merge_transitions_flag]]]"
    echo "Flag is SET if equal to $FLAG_SET, and not set if not given or different. "
else
    echo "Trimming and coloring supplied .dot model"
    trim $1 $2 $3
fi 



