#!/usr/bin/env bash
# Script for improving the visuals of a learned model

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
readonly SCRIPT_DIR
readonly EXP_SCRIPTS_DIR="$SCRIPT_DIR/experiments/scripts/"
readonly COLOR_FILE="$EXP_SCRIPTS_DIR/coloredPaths.json"
readonly REPL_FILE="$EXP_SCRIPTS_DIR/replacements.json"

# configurable options
opt_prune_states=0
opt_output=""
opt_merge_transitions=0
opt_disable_dottrimmer=0
opt_export=0
opt_view=0

# transforms capitalized snake case (CLIENT_HELLO) to came case (ClientHello)
function to_camel() {
    sed -i -r 's/([A-Z])([A-Z]*)_*/\1\L\2/g' "$1"
}

# shortens state labels from (for example) s3 to 3
function shorten_state_labels() {
    sed -i -r 's/\"s([0-9])/\"\1/g' "$1"
}

# shortens messages in capitalized camel case according to the USENIX DTLS server work conventions
# colors valid handshake-completing paths, where the paths are given in $COLOR_FILE
# optionally, removes from the model states from which handshake-completing states cannot be reached
function relabel_and_color() {
    input_model=$1
    relabelled_model=$2

    echo "Formatting model by relabeling and coloring handshakes"
    extra=''
    other_thr=3
    if [[ $opt_prune_states -eq 1 ]]; then
        echo "State pruning enabled"
        extra='-ego "\s*.*App.*"'
        other_thr=100
    else
        echo "State pruning disabled"
        extra=''
    fi
    echo java -jar "$EXP_SCRIPTS_DIR"/dot-trimmer.jar -r "$REPL_FILE" -i "$input_model" -o "$relabelled_model" -t "$other_thr" -pc "$COLOR_FILE" "$extra"
    java -jar "$EXP_SCRIPTS_DIR"/dot-trimmer.jar -r "$REPL_FILE" -i "$input_model" -o "$relabelled_model" -t "$other_thr" -cp "$COLOR_FILE" "$extra"
}

# merges (edges of) transitions connecting the same states by placing them on the same arrow in stacked formation
function merge_transitions() {
    input_model=$1
    condensed_model=$2

    echo "Formatting model by merging edges of transitions connecting same states"
    orig_edge_num=$(grep -ce '->' "$input_model")
    python3 "$EXP_SCRIPTS_DIR"/dotformat.py "$input_model" "$condensed_model"
    condensed_edge_num=$(grep -ce '->' "$condensed_model")
    echo "Reduced the number of edges from $orig_edge_num to $condensed_edge_num"
}

# makes a model more readable by shortening the labels, coloring important paths in it, and optionally prunning superfluous states and merging transitions connecting the same states
function trim() {
    model=$1
    model_name=$(basename "$model")
    model_dir=$(dirname "$model")
    smodel=$model_dir/$model_name.simplified.dot
    cp "$model" "$smodel"
    to_camel "$smodel"
    
    if [[ -n $opt_output ]]; then
        tmodel=$opt_output
    else
        tmodel=$model_dir/$model_name.trimmed.dot
    fi
    
    if [[ $opt_disable_dottrimmer -eq 1 ]]; then
        echo "dot-trimmer functionality is disabled, hence coloring/label replacement/state prunning will not be applied"
        cp "$smodel" "$tmodel"
    else
        relabel_and_color "$smodel" "$tmodel"
    fi

    if [[ $opt_merge_transitions -eq 1 ]] ; then
        merge_transitions "$tmodel" "$tmodel"
    fi

    shorten_state_labels "$tmodel"
    rm "$smodel"

    if [[ $opt_export -eq 1 && -f $tmodel ]] ; then
        pdffile=${tmodel%dot}pdf
        dot -Tpdf "$tmodel" > "$pdffile"
    fi

    if [[ $opt_view -eq 1 && -f $tmodel ]] ; then 
        xdot "$tmodel"
    fi
}

if [ $# = 0 ]; then 
    echo "Usage trim_model.sh dot_model [-o|--output dot_output] [-ps|--prune-states] [-mt|--merge-transitions] [-dd|--disable-dottrimmer] [-e|--export] [-v|--view] dot_model"
else
    while [[ "$1" =~ ^- ]]; do case $1 in
        -ps | --prune-states )
            opt_prune_states=1
            ;;
        -mt | --merge-transitions )
            opt_merge_transitions=1
            ;;
        -dd | --disable-dottrimmer )
            opt_disable_dottrimmer=1
            ;;
        -o | --output )
            shift; opt_output=$1
            ;;
        -e | --export )
            opt_export=1
            ;;
        -v | --view )
            opt_view=1
            ;;
        * )
            echo "Unsupported option $1"
            return
            ;;
            esac
            shift
        done

    if [[ ! $# -eq 1 ]]; then
        echo "Expected a single parameter after options"
        return
    fi

    if [[ ! -e $1 ]]; then
        echo "File $1 does not exist"
        return
    fi

    echo "Trimming and coloring $1"
    trim "$1"
fi 

