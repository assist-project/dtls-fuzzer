This folder contains key material (keys/certs/JKS keystores) used during learning experiments.
The key material has to be consistent with the explicit keypairs which appear in the TLS-Attacker configuration files used by DTLS-Fuzzer.
If that's not the case, client authentication will fail.
Key material can be re-generated using the [gen_keys.sh](../scripts/gen_keys.sh) script.
For no particular reason, the java key store is protected by password "password", and is under the name "DTLS-Fuzzer".
