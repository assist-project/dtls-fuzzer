#!/usr/bin/env bash
#
# Installs some necessary packages and then installs DTLS-Fuzzer.

# SCRIPT_DIR should correpond to dtls-fuzzer's root directory
readonly SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
readonly PATCHES_DIR="$SCRIPT_DIR/experiments/patches"

# this version should be the same as that in pom.xml
readonly PROTOCOLSTATEFUZZER_COMMIT="9927e7bc"
readonly PROTOCOLSTATEFUZZER_REP_URL="https://github.com/protocol-fuzzing/protocol-state-fuzzer.git"
readonly PROTOCOLSTATEFUZZER_FOLDER="ProtocolState-Fuzzer"
readonly PROTOCOLSTATEFUZZER_PATCH="$PATCHES_DIR/protocolstate-fuzzer-9927e7bc.patch"

function check_java() {
    if ! command -v java &> /dev/null
    then
        echo "JDK is not installed ('java' command not in PATH)"
        if command -v apt-get &> /dev/null
        then
            echo "Installing java using apt-get"
            sudo apt-get install openjdk-11-jdk
        else
            echo "Install JDK >= 11, add it to PATH and re-run"
            exit
        fi
    else
        java_vm=$(java -version 2>&1 >/dev/null | grep -o "Server VM\|Client VM")
        if [[ ! $java_vm == "Server VM" ]]
        then
            echo "Required Java Server VM (a JDK instead of JRE), found $java_vm"
            echo "Install JDK >= 11, add it to PATH and re-run"
            exit
        fi
    fi
} 

function check_mvn() {
    if ! command -v mvn &> /dev/null
    then
        echo "maven is not installed ('mvn' command not in PATH)"
        if command -v apt-get &> /dev/null
        then
            echo "Installing maven using apt-get"
            sudo apt-get install maven
        else
            echo "Install Apache maven, add it to PATH and re-run"
            exit 
        fi
    fi
}

function clone_rep() {
    sut_dir=$1
    rep_url=$2
    rep_com=$3
    echo "Cloning repository $rep_url commit $rep_com to $sut_dir"
    git clone $rep_url $sut_dir
    if [[ -n "$rep_com" ]]; then
        ( cd $sut_dir ; git checkout $rep_com ) #; rm -rf $sut_dir/.git )
    fi
}

function install_protocolstatefuzzer() {
    clone_rep $PROTOCOLSTATEFUZZER_FOLDER $PROTOCOLSTATEFUZZER_REP_URL $PROTOCOLSTATEFUZZER_COMMIT
    (
        cd $PROTOCOLSTATEFUZZER_FOLDER
        echo "Patching ProtocolState-Fuzzer for compatibility with Java 11"
        git apply $PROTOCOLSTATEFUZZER_PATCH
        echo "Installing ProtocolState-Fuzzer"
        mvn install
    )
}

# Check for the presence of Java and maven and if 'apt-get' is present, install them
check_java
check_mvn

# Install ProtocolState-Fuzzer before patching for Java 11
install_protocolstatefuzzer

# Install DTLS-Fuzzer
echo "Installing DTLS-Fuzzer..."
mvn clean install
