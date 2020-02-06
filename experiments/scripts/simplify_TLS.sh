#TODO Add parameter

MODELS_DIR="new_models/"
SCRIPTS_DIR="."
COLOR_FILE=coloredPaths.json
REPL_FILE=replacements.json

# reverse sed 's/\([A-Z][a-z0-9]*\)/\U\1_/g'

# transforms capitalized snake case (CLIENT_HELLO) to came case (ClientHello)
function toCamel() {
    sed -i -r 's/([A-Z])([A-Z]*)_*/\1\L\2/g' $1
}

# transforms camel case (ClientHello) to capitals (CH) for ultimate trimmage
function toCapitals() {
    sed -i -r 's/([A-Z])[a-z]*/\1/g' $1
}

# shortens state labels from (for example) s3 to 3
function shortenStateLabels() {
    sed -i -r 's/\"s([0-9])/\"\1/g' $1
}

function fullTrim() {
    extra=''
    if [ $3 ]; then 
        extra='-ego "\s*App"'
    else
        extra=''
    fi
    echo java -jar $SCRIPTS_DIR/dot-trimmer.jar -r $SCRIPTS_DIR/$REPL_FILE -i $1 -o $2 -t 3 -pc $COLOR_FILE $extra
    java -jar $SCRIPTS_DIR/dot-trimmer.jar -r $SCRIPTS_DIR/$REPL_FILE -i $1 -o $2 -t 3 -pc $COLOR_FILE $extra
}

# trimming used in the model
#function selectiveTrim() {
#    sed -i -r 's/Timeout/T/g' $1
#    sed -i -r 's/ChangeCipherSpec/CCS/g' $1
#    sed -i -r 's/(Fatal\,)|(Warning\,)//g' $1
#}

function format() {
    echo Formatting $1; python $SCRIPTS_DIR/dotformat.py $1 $2
}

function simplify() {
    model=$1
    smodel=${model%dot}simplified.dot
    cp $model $smodel
    toCamel $smodel
    #stmodel=${model%dot}strimmed.dot $2
    #selectiveTrim $smodel $stmodel
    #format $stmodel $stmodel
    if [ $2 ]; then 
        tmodel=${model%dot}reduced.dot
    else
        tmodel=${model%dot}trimmed.dot
    fi
    fullTrim $smodel $tmodel $2
    format $tmodel $tmodel
    shortenStateLabels $tmodel
    rm $smodel 
}

if [ $# = 0 ]; then 
    echo "Removing existing simplified .dot files in models directory"
    rm $MODELS_DIR/*simplified*dot
    rm $MODELS_DIR/*formatted*dot
    rm $MODELS_DIR/*trimmed*dot
    echo "Simplifying formatted .dot files in models directory"
    for dot_file in $MODELS_DIR/*.dot; do
        echo $dot_file
        simplify $dot_file
    done
else
    echo "Simplifying supplied .dot model"
    simplify $1 $2
fi 



