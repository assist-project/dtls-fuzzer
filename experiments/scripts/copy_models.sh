
readonly SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
readonly EXP_DIR=${SCRIPT_DIR%"/scripts"}
readonly RESULTS_DIR="$EXP_DIR/results"
readonly MODELS_DIR="$EXP_DIR/models"

function copy_models() {
    side=$1
    side_results_dir="$RESULTS_DIR/$side"
    side_models_dir="$MODELS_DIR/$side"
    mkdir -p $side_models_dir
    for learned_dir in $side_results_dir/*rwalk*; do
        learned_dirname=$(basename $learned_dir)
        learned_model="$learned_dir/learnedModel.dot"
        if [[ -f $learned_model ]]; then
            model_dirname="$learned_dirname"
            model_dirname=${model_dirname//_rwalk/}
            model_dirname=${model_dirname//_incl/}
            model_dir="$side_models_dir/$model_dirname"
            mkdir -p $model_dir
            cp $learned_model $model_dir
            cp "$learned_dir/alphabet.xml" $model_dir
            cp "$learned_dir/command.args" $model_dir
        fi
    done
}

copy_models "servers"
copy_models "clients"