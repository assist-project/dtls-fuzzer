#!/usr/bin/env bash

if [ $# = 0 ]; then
    alpha="../examples/alphabet.xml"
else
    alpha="$1"
fi

java -cp "TLS-ModelBasedTester.jar:lib/*" se.uu.it.modeltester.Main -cmd "openssl s_server -key /home/pfg666/Keys/RSA1024/server-key.pem -cert /home/pfg666/Keys/RSA1024/server-cert.pem -accept 20001 -dtls -mtu 1500" -onlyLearn -protocol DTLS12 -connect localhost:20001 -runWait 100 -timeout 10 -randLength 5 -queries 20000 -alphabet "$alpha"
