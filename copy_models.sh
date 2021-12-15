
readonly SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
readonly MAX_HYP_NUM=1000

opt_excl=''
opt_incl=''
opt_delete=0
opt_merge=0
opt_build_trimmed=0 # if not 0, builds a trimmed version of the model
opt_build_reduced=0 # if not 0, builds a reduced version of the model
opt_incl_error=0 # if not 0, includes experiments which ended in error (their dir contains the error.msg file)
opt_max_states=100000 # for learning experiments that have not converged select the latest hyp with at most this number of states

function num_states() {
    model=$1
    num=` (grep -c circle $model)`
    echo $num
}

function copy_models() {
    experiments_dir=$1
    models_dir=$2
    mkdir -p $models_dir
    for exp_dir in $experiments_dir/*; do
        exp_dirname=$(basename $exp_dir)
        learned_model="$exp_dir/learnedModel.dot"
        selected_model="$exp_dir/selectedModel.dot"
        learned_model_alphabet="$exp_dir/alphabet.xml"
        exp_sulconfig="$exp_dir/sul.config"

        incorrect="$exp_dir/incorrect_model"
        if [[ -f $incorrect ]]; then 
            echo "Ignoring $exp_dirname because of the presence of $incorrect"
            continue
        fi

        error="$exp_dir/error.msg"
        if [[ $opt_incl_error -eq 0 && -f $error ]]; then 
            echo "Ignoring $exp_dirname because of the presence of $error"
            continue
        fi

        if [[ $opt_excl -eq 1 && $exp_dirname =~ $opt_excl ]]; then 
            echo "Ignoring $exp_dirname  due to the exclude option"
            continue
        fi 
        
        if [[ $opt_incl -eq 1 && ! $exp_dirname =~ $opt_incl ]]; then 
            echo "Ignoring $exp_dirname  due to the include (only) option"
            continue
        fi

        if [[ ! -f $learned_model ]]; then
            if [[ -f $selected_model ]]; then 
                learned_model=$selected_model
            else 
                last_hyp_num=` (ls $exp_dir | grep -c hyp*)`
                if [[ $last_hyp_num  -gt $MAX_HYP_NUM ]]; then
                    last_hyp_num=$MAX_HYP_NUM
                fi
                last_hyp="$exp_dir/hyp$last_hyp_num.dot"
                learned_model=$last_hyp
            fi
        fi
        if [[ -f $learned_model ]]; then
            sutconfig_name="$exp_dirname"
            sutconfig_name=${sutconfig_name//_rwalk/}
            sutconfig_name=${sutconfig_name//_stests/}
            sutconfig_name=${sutconfig_name//_incl/}
            model_dir="$models_dir/$sutconfig_name"
            mkdir -p $model_dir

            cp $learned_model_alphabet "$model_dir/alphabet.xml" 
            cp $exp_sulconfig "$model_dir/sul.config"
            
            model="$model_dir/model.dot"
            cp $learned_model $model
            
            if [[ opt_build_trimmed -eq 1 ]]; then 
                trimmed_model="$model_dir/trimmed.dot"
                n_states=$(num_states $learned_model)
                if [[ $n_states -le 100 ]]; then  
                    timeout 5s bash $SCRIPT_DIR/trim_model.sh -mt -e -o $trimmed_model $model
                fi
            fi

            if [[ opt_build_reduced -eq 1 ]]; then 
                reduced_model="$model_dir/reduced.dot"
                bash $SCRIPT_DIR/trim_model.sh -ps -mt -e -o $reduced_model $model
            fi
        fi
    done
}


if [[ $# = 0 || $# = 1 ]]; then
    echo "Usage: copy_models.sh  [options] exp_dir to_dir..."
    echo "Supported options:"
    echo "-m | --merge  : in case to_dir points to an existing folder, keep it and place the model folders in it (potentially overwriting existing ones)"
    echo "-d | --delete  : in case to_dir points to an existing folder, delete it and continue as per usual"
    echo "-bt | --build-trimmed  : build trimmed versions of the models"
    echo "-br | --build-reduced  : build reduced versions of the models"
    echo "-ie | --include-error  : include experiments which ended in error"
    exit 0
fi



while [[ "$1" =~ ^- ]]; do case $1 in
-e | --exclude )
    shift
    opt_excl=$1
    ;;
-i | --include )
    shift
    opt_incl=$1
    ;;
-d | --delete )
    opt_delete=1
    ;;
-m | --merge )
    opt_merge=1
    ;;
-bt | --build-trimmed )
    opt_build_trimmed=1
    ;;
-br | --build-reduced )
    opt_build_reduced=1
    ;;
-ie | --include-error )
    opt_incl_error=1
    ;;
* )
    echo "unsupported option $1"
    return
    ;;
esac; shift; done 


if [[ -d $2 ]]; then 
    if [[ opt_delete -eq 1 ]]; then 
        rm -r $2
    elif [[ opt_merge -eq 1 ]]; then
        echo "Will merge with existing"
    else
        echo "Model folder already exists at $2. Delete it first and re-run or merge (-m)"
        exit 1
    fi
 fi

copy_models $1 $2
