#!/bin/sh
for file in "$@"
do
    simFile="sim""$file"
    echo "$simFile"
    cp "$file" "$simFile"
    sed -i -E 's/GenericMessageWord\{message\=(.*)\}/\1/g' "$simFile"
    sed -i -E 's/ResponseFingerprint\[.*Messages\=\[(.+)\,\]\,\sRecc.*\"/\1\"/g' "$simFile"
    sed -i -E 's/ResponseFingerprint\[.*Messages\=\[\]\,\sRecc.*\"/TIMEOUT\"/g' "$simFile"
    sed -i -E 's/FinishedWord\{\}/FINISHED/g' "$simFile"
    sed -i -E 's/ChangeCipherSpecWord\{\}/CHANGE_CIPHER_SPEC/g' "$simFile"
    sed -i -E 's/ClientHelloWord\{.*\}/CLIENT_HELLO/g' "$simFile"
done
