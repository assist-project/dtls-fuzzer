#!/bin/sh
# Script used to prune learned (D)TLS models inferred using TLS-StateVulnFinder
# in a format that is understantable by the TLS-ModelFuzz tool.
# For whatever reason, I couldn't get [^x] (anything but character x) to work in sed, 
# otherwise expressions would have been simpler

function apply_changes() {
    sed -i -E 's/GenericMessageWord\{message\=([a-zA-Z0-9_\-\,\(\)]*)\}/\1/g' $1
    sed -i -E 's/ResponseFingerprint\[.*Messages\=\[(.+)\,\]\,\sRecc.*\"/\1\"/g' $1
    sed -i -E 's/ResponseFingerprint\[.*Messages\=\[\]\,\sRecc.*\"/TIMEOUT\"/g' $1
    sed -i -E 's/FinishedWord\{\}/FINISHED/g' $1
    sed -i -E 's/ChangeCipherSpecWord\{\}/CHANGE_CIPHER_SPEC/g' $1
    sed -i -E 's/ClientHelloWord\{suite\=TLS_PSK.*\}/PSK_CLIENT_HELLO/g' $1
    sed -i -E 's/ClientHelloWord\{suite\=TLS_RSA.*\}/RSA_CLIENT_HELLO/g' $1
}


for file in "$@"
do
    file_name=${file##*/}
    file_dir=${file%$file_name}
    if [ ${#file_dir} = 0 ]; then
        file_dir="."
    fi
    sim_file_name="sim"$file_name
    sim_file=$file_dir"/"$sim_file_name
    cp $file $sim_file
    apply_changes $sim_file
done
