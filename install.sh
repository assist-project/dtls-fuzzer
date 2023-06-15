#!/usr/bin/env bash
#
# Installs some necessary packages and then installs DTLS-Fuzzer.

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

# Check for the presence of Java and maven and if 'apt-get' is present, install them
check_java
check_mvn


# Install DTLS-Fuzzer
echo "Installing DTLS-Fuzzer..."
mvn clean install
