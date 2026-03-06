#!/usr/bin/env bash

# both mealy_arguments and ra_arguments are passed to run_experiments, just as arrays.
# shellcheck disable=SC2034
mealy_experiments=(
    "args/mealy/ctinydtls/learn_ctinydtls_client_ecdhe_cert"
    "args/mealy/ctinydtls/learn_ctinydtls_client_ecdhe_cert_shorths"
    "args/mealy/ctinydtls/learn_ctinydtls_client_psk"
    "args/mealy/ctinydtls/learn_ctinydtls_client_psk_shorths"
    "args/mealy/ctinydtls/learn_ctinydtls_server_psk"
    "args/mealy/etinydtls/learn_etinydtls_client_ecdhe_cert"
    "args/mealy/etinydtls/learn_etinydtls_client_psk"
    "args/mealy/etinydtls/learn_etinydtls_server_psk"
    "args/mealy/jsse/learn_jsse_client_ecdhe_cert"
    "args/mealy/jsse/learn_jsse_server_ecdhe_cert_req"
    "args/mealy/mbedtls/learn_mbedtls_client_dhe_ecdhe_rsa_cert"
    "args/mealy/mbedtls/learn_mbedtls_client_dhe_ecdhe_rsa_cert_reneg"
    "args/mealy/mbedtls/learn_mbedtls_client_ecdhe_cert_reneg"
    "args/mealy/mbedtls/learn_mbedtls_client_psk"
    "args/mealy/mbedtls/learn_mbedtls_client_psk_reneg"
    "args/mealy/mbedtls/learn_mbedtls_server_all_cert_req"
    "args/mealy/scandium/learn_scandium_client_ecdhe_cert"
    "args/mealy/scandium/learn_scandium_client_psk"
    "args/mealy/scandium/learn_scandium_server_ecdhe_cert_req"
    "args/mealy/scandium/learn_scandium_server_psk"
    "args/mealy/wolfssl/learn_wolfssl_client_psk"
    "args/mealy/wolfssl/learn_wolfssl_server_psk"
)

ra_experiments=(
    "args/ra/ctinydtls/learn_ctinydtls_client_ecdhe_cert"
    "args/ra/ctinydtls/learn_ctinydtls_client_psk"
    "args/ra/ctinydtls/learn_ctinydtls_client_ecdhe_cert_shorths"
    "args/ra/ctinydtls/learn_ctinydtls_client_psk_shorths"
    "args/ra/ctinydtls/learn_ctinydtls_server_psk"
    "args/ra/etinydtls/learn_etinydtls_client_ecdhe_cert"
    "args/ra/etinydtls/learn_etinydtls_client_psk"
    "args/ra/etinydtls/learn_etinydtls_server_psk"
    "args/ra/jsse/learn_jsse_client_ecdhe_cert"
    "args/ra/jsse/learn_jsse_server_ecdhe_cert_req"
    "args/ra/mbedtls/learn_mbedtls_client_dhe_ecdhe_rsa_cert_reneg"
    "args/ra/mbedtls/learn_mbedtls_client_ecdhe_cert_reneg"
    "args/ra/mbedtls/learn_mbedtls_server_all_cert_req"
    "args/ra/mbedtls/learn_mbedtls_client_psk"
    "args/ra/mbedtls/learn_mbedtls_client_psk_reneg"
    "args/ra/mbedtls/learn_mbedtls_client_dhe_ecdhe_rsa_cert"
    "args/ra/scandium/learn_scandium_client_psk"
    "args/ra/scandium/learn_scandium_server_ecdhe_cert_req"
    "args/ra/scandium/learn_scandium_server_psk"
    "args/ra/scandium/learn_scandium_client_ecdhe_cert"
    "args/ra/wolfssl/learn_wolfssl_client_psk"
    "args/ra/wolfssl/learn_wolfssl_server_psk"

)

NO_SIMUL_EXPERIMENTS=3

MEALY_JAR="target/dtls-fuzzer-0.3-SNAPSHOT-mealy-jar-with-dependencies.jar"

RA_JAR="target/dtls-fuzzer-0.3-SNAPSHOT-ra-jar-with-dependencies.jar"

function run_experiments() {
    local -n experiments="$1"
    local no_parallell="$2"
    local jarfile="$3"

    for experiment in "${experiments[@]}"; do
        if [[ $(jobs -r | wc -l) -ge ${no_parallell} ]]; then
            wait -n
        fi
        (java -jar "${jarfile}" "${experiment}") & # Actually run experiments
    done
    wait
}


run_experiments mealy_experiments $NO_SIMUL_EXPERIMENTS $MEALY_JAR
run_experiments ra_experiments $NO_SIMUL_EXPERIMENTS $RA_JAR
