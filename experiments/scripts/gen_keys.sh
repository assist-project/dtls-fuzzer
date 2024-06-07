#!/usr/bin/env bash
# This script updates the certificates used by DTLS-Fuzzer.
# It is based on the 'keygen.sh' script of TLS-Attacker, see https://github.com/tls-attacker/TLS-Attacker/tree/main/TLS-Core/src/main/resources .
# For PEM to JKS conversion, read see: https://www.baeldung.com/convert-pem-to-jks

readonly DAYS_VALIDITY=5000 
readonly PASSWORD="password"
output_folder=$1

# Creates JKS store from PEM key and certificate pair.
function build_jks() {
    key=$1
    cert=$2
    jks=$3
    pkcs12=$jks.p12
    # Create exporting key and cert PEM files to a PKCS12 file.
    openssl pkcs12 -export -in "$cert" -inkey "$key" -out "$pkcs12" -name "DTLS-Fuzzer" -passout "pass:$PASSWORD"
    # Importing from the PKCS12 onto a JKS file.
    keytool -importkeystore -srckeystore "$pkcs12" -srcstoretype pkcs12 -destkeystore "$jks" -deststoretype JKS -storepass "$PASSWORD" -noprompt -keypass "$PASSWORD" -srcstorepass "$PASSWORD"
    # Remove PKCS12 file
    rm "$pkcs12"
}



if [ "$#" != 1 ]; then
    echo "Usage: ${0##*/} output_folder"
    echo "Creates key and cert files in PEM and JKS format, and stores them in the specified directory"
    exit 1
fi

if [[ ! -d "$output_folder" ]]; then
    mkdir "$output_folder"
fi

# 
for len in 2048 4096
do
    openssl genpkey -algorithm RSA -out "$output_folder/rsa${len}_key.pem" -pkeyopt rsa_keygen_bits:"${len}"
    openssl req -key "$output_folder/rsa${len}_key.pem" -new -x509 -days "$DAYS_VALIDITY" -out "$output_folder/rsa${len}_cert.pem" -subj "/CN=dtls-fuzzer.com"
    build_jks "$output_folder/rsa${len}_key.pem" "$output_folder/rsa${len}_cert.pem" "$output_folder/rsa${len}.jks"
done

# Maps curve names as according to RFC 5480 to those understood by OpenSSL.
# Often same names are used, but sometimes they can be different (e.g., OpenSSL refers to ecp256r1 by prime256v1).
# This is because OpenSSL uses RFC 3279 naming, which was updated by RFC 5480 (check the latter for details).
function map_curve() {
    curve=$1
    if [[ "$curve" = "secp256r1" ]]; then
        echo "prime256v1"
    else
        echo "$curve"
    fi
}


for named_curve in secp256r1 secp256k1
do
    openssl_curve=$(map_curve ${named_curve})
    openssl ecparam -name "${openssl_curve}" -genkey -out "$output_folder/ec_${named_curve}_key.pem"
    openssl req -key "$output_folder/ec_${named_curve}_key.pem" -new -x509 -days "$DAYS_VALIDITY" -out "$output_folder/ec_${named_curve}_cert.pem" -subj "/CN=dtls-fuzzer.com"
    build_jks "$output_folder/ec_${named_curve}_key.pem" "$output_folder/ec_${named_curve}_cert.pem" "$output_folder/ec_${named_curve}.jks"
done
