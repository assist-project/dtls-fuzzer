#!/usr/bin/env bash
#
# Installs some necessary packages and then installs DTLS-Fuzzer.

# SCRIPT_DIR should correspond to dtls-fuzzer's root directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
readonly SCRIPT_DIR
readonly PATCHES_DIR="$SCRIPT_DIR/experiments/patches"

readonly PROTOCOLSTATEFUZZER_COMMIT="68e0eea"
readonly PROTOCOLSTATEFUZZER_REP_URL="https://github.com/protocol-fuzzing/protocol-state-fuzzer.git"
readonly PROTOCOLSTATEFUZZER_FOLDER="ProtocolState-Fuzzer"
readonly PROTOCOLSTATEFUZZER_PATCH="$PATCHES_DIR/protocolstate-fuzzer-$PROTOCOLSTATEFUZZER_COMMIT.patch"

readonly TLSATTACKER_COMMIT="v5.1.6"
readonly TLSATTACKER_REP_URL="https://github.com/tls-attacker/TLS-Attacker.git"
readonly TLSATTACKER_FOLDER="TLS-Attacker"
readonly TLSATTACKER_PATCH="$PATCHES_DIR/TLS-Attacker-$TLSATTACKER_COMMIT.patch"

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
    echo     git clone "$rep_url" "$sut_dir"
    git clone "$rep_url" "$sut_dir"
    if [[ -n "$rep_com" ]]; then
        ( cd "$sut_dir" || exit ; git checkout "$rep_com" )
    fi
}

function install_protocolstatefuzzer() {
    echo $PROTOCOLSTATEFUZZER_FOLDER
    if [[ -d $PROTOCOLSTATEFUZZER_FOLDER ]]; then
        echo "$PROTOCOLSTATEFUZZER_FOLDER folder already exists"
        echo "Skipping ProtocolState-Fuzzer setup"
    else
        clone_rep $PROTOCOLSTATEFUZZER_FOLDER $PROTOCOLSTATEFUZZER_REP_URL $PROTOCOLSTATEFUZZER_COMMIT
        (
            cd $PROTOCOLSTATEFUZZER_FOLDER || exit
            echo "Patching ProtocolState-Fuzzer for compatibility with Java 11"
            git apply "$PROTOCOLSTATEFUZZER_PATCH"
            echo "Installing ProtocolState-Fuzzer"
            mvn install -DskipTests
        )
    fi
}

function install_tlsattacker() {
    echo $TLSATTACKER_FOLDER
    if [[ -d $TLSATTACKER_FOLDER ]]; then
        echo "$TLSATTACKER_FOLDER folder already exists"
        echo "Skipping TLS-Attacker setup"
    else
        clone_rep "$TLSATTACKER_FOLDER" "$TLSATTACKER_REP_URL" "$TLSATTACKER_COMMIT"
        (
            cd $TLSATTACKER_FOLDER || exit
            echo "Patching TLS-Attacker to remove deplicate discard and fragment reordering."
            git apply "$TLSATTACKER_PATCH"
            echo "Installing TLS-Attacker"
            mvn install -DskipTests
        )
    fi
}

# Check for the presence of Java and maven and if 'apt-get' is present, install them
check_java
check_mvn

# Checkout ProtocolState-Fuzzer repo, apply Java 11 compatibility patch, and install the library
install_protocolstatefuzzer

# Checkout TLS-Attacker repo, patch it and install the library
install_tlsattacker

# Install DTLS-Fuzzer
echo "Installing DTLS-Fuzzer..."
mvn clean install
