#!/bin/bash
readonly SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
readonly SUTS_DIR="$SCRIPT_DIR/suts"
readonly MBEDTLS_URL='https://tls.mbed.org/download/start/mbedtls-2.16.1-gpl.tgz'
readonly OPENSSL_URL='https://www.openssl.org/source/old/1.1.1/openssl-1.1.1b.tar.gz'
readonly ETINYDTLS_REP_URL='https://github.com/eclipse/tinydtls'
readonly ETINYDTLS_COMMIT='8414f8a'
readonly CTINYDTLS_REP_URL='https://github.com/contiki-ng/tinydtls'
readonly CTINYDTLS_COMMIT='53a0d97'

sut_strings=("openssl" "mbedtls" "etinydtls" "ctinydtls" "jsse-11" "scandium-old" "scandium-new" "gnutls-old" "gnutls-new")

if [ $# = 0 ]; then
    echo "Usage: bash setup_sut.sh sut"
    echo "Where sut is an element in: "
    for ix in ${!sut_strings[*]}
    do
        printf "   %s\n" "${sut_strings[$ix]}"
    done
    exit 1
else
    echo "good"
    #if [[ ! " ${sut_strings[@]} " =~ " ${value} " ]]; then
    #port="$1"
fi
