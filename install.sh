#!/usr/bin/env bash
#
# Installs some necessary packages to build the tool
# and then installs DTLS-Fuzzer.

# SCRIPT_DIR should correpond to DTLS-Fuzzer's root directory
readonly SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
readonly PATCHES_DIR="$SCRIPT_DIR/experiments/patches"

function arch_temp_dir() {
	arch_url=$1
	temp_dir=/tmp/`(basename $arch_url)`
	echo $temp_dir
}

# we can afford to copy/paste since this code likely will not change
function solve_arch() {
    arch_url=$1
    target_dir=$2
    temp_dir=`arch_temp_dir $arch_url`
    echo $temp_dir
    echo "Fetching/unpacking from $arch_url into $target_dir"
    if [[ ! -f "$temp_dir" ]]
    then
        echo "Downloading archive from url to $temp_dir"
        wget -nc --no-check-certificate $arch_url -O $temp_dir
    fi
    
    mkdir $target_dir
    # ${temp_dir##*.} retrieves the substring between the last index of . and the end of $temp_dir
    arch=`echo "${temp_dir##*.}"`
    if [[ $arch == "xz" ]]
    then
        tar_param="-xJf"
    else 
        tar_param="zxvf"
    fi
    echo $tar_param
    if [ $target_dir ] ; then
        tar $tar_param $temp_dir -C $target_dir --strip-components=1
    else 
        tar $tar_param $temp_dir
    fi
}

function check_java() {
    if ! command -v java &> /dev/null
    then
        echo "JDK is not installed ('java' command not in PATH)"
        if command -v apt-get &> /dev/null
        then
            echo "Installing java using apt-get"
            sudo apt-get install openjdk-8-jdk
        else
            echo "Install JDK 8, add it to PATH and re-run"
            exit
        fi
    else
        java_ver=`java -version 2>&1 >/dev/null | grep "java version\|openjdk version"  | awk '{print $3}' | tr -d '"'`
        
#        if [[ ! $java_ver == 1.8* ]]
#        then
#            echo "Required Java version 1.8, actual version $java_ver"
#            echo "Install JDK 8, add it to PATH and re-run"
#            exit
#        fi
        
        java_vm=`java -version 2>&1 >/dev/null | grep -o "Server VM\|Client VM"`
        if [[ ! $java_vm == "Server VM" ]]
        then
            echo "Required Java Server VM (a JDK instead of JRE), found $java_vm"
            echo "Install JDK 8, add it to PATH and re-run"
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

# we check for the presence of Java and maven and if 'apt-get' is present, install them
check_java
check_mvn


# Install DTLS-Fuzzer
echo "Installing DTLS-Fuzzer..."
mvn clean install
