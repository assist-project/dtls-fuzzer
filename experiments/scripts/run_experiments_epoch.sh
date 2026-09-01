#!/usr/bin/env bash

# both mealy_arguments and ra_arguments are passed to run_experiments, just as arrays.
# shellcheck disable=SC2034



ARGS_SERVER="-testFile examples/tests/ra/servers/psk_epoch -roundLimit 1"
ARGS_CLIENT="-testFile examples/tests/ra/clients/psk_epoch -roundLimit 1"
ARGS_SLLAMBDA="${ARGS_GLOBAL} -Doutput.dir=output/ra-sllambda -learningAlgorithm SLLAMBDA"
ARGS_SLSTAR="${ARGS_GLOBAL} -Doutput.dir=output/ra-slstar -learningAlgorithm SLSTAR"
ARGS_SLLEQ="${ARGS_GLOBAL} -Doutput.dir=output/ra-slleq -learningAlgorithm SLLEQ"

ALGORITHMS=(
    "SLLAMBDA"
    "SLSTAR"
    "SLLEQ"
)

RA_EXPERIMENTS=(
    "args/ra/etinydtls/learn_etinydtls_server_psk_epoch"
    "args/ra/mbedtls/learn_mbedtls_server_psk_epoch"
    "args/ra/scandium/learn_scandium_server_psk_epoch"
    "args/ra/wolfssl/learn_wolfssl_server_psk_epoch"
    "args/ra/openssl/learn_openssl_server_psk_epoch"
    "args/ra/piondtls/learn_piondtls_server_psk_epoch"
)




function gen_exp_for_each_alg() {
	# -n is a name reference, otherwise variable is assigned a value
    local -n input="$1"
    local -n output="$2"
    # additional arguments
    local extra_args="$3"

    output=()

    for experiment in "${input[@]}"; do
        for alg in "${ALGORITHMS[@]}"; do
            output+=("${experiment} -Doutput.ra=output/ra-${alg} -learningAlgorithm ${alg} ${extra_args}")
        done
    done
}

NO_SIMUL_EXPERIMENTS=1
RA_JAR="target/dtls-fuzzer-0.3-SNAPSHOT-ra-jar-with-dependencies.jar"

# Optional filter pattern from command line
EXPERIMENT_FILTER="${1:-}"

function run_experiments() {
    local -n experiments="$1"
    local no_parallel="$2"
    local jarfile="$3"
    local filter="$4"

    for experiment in "${experiments[@]}"; do
        # Skip experiments that do not match the filter
        if [[ -n "$filter" && ! "$experiment" == $filter ]]; then
            continue
        fi

        if [[ $(jobs -r | wc -l) -ge ${no_parallel} ]]; then
            wait -n
        fi
        echo $experiment
        (java -jar "${jarfile}" ${experiment}) &
    done

    wait
}

gen_exp_for_each_alg RA_EXPERIMENTS actual_experiments "${ARGS_SERVER}"

echo "The following experiments are considered"
printf '%s\n' "${actual_experiments[@]}"

run_experiments \
    actual_experiments \
    "$NO_SIMUL_EXPERIMENTS" \
    "$RA_JAR" \
    "$EXPERIMENT_FILTER"
