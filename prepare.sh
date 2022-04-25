#!/usr/bin/env bash
#
# Deploy all dependencies for installing DTLS-Fuzzer.

# SCRIPT_DIR should correpond to dtls-fuzzer's root directory
readonly SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
readonly PATCHES_DIR="$SCRIPT_DIR/experiments/patches"

# this version should be the same as that in pom.xml
readonly TLSATTACKER_VER="3.2b"
readonly TLSATTACKER_ARCH_URL="https://github.com/RUB-NDS/TLS-Attacker/archive/$TLSATTACKER_VER.tar.gz"
readonly TLSATTACKER_FULLNAME="TLS-Attacker-$TLSATTACKER_VER"

# location to store the downloaded archive file
readonly TLSATTACKER_TEMP_ARCH="$SCRIPT_DIR/$TLSATTACKER_FULLNAME.tar.gz"
readonly TLSATTACKER_DIR="$SCRIPT_DIR/$TLSATTACKER_FULLNAME"
readonly TLSATTACKER_PATCH="$PATCHES_DIR/$TLSATTACKER_FULLNAME.patch"

function arch_temp_dir() {
	arch_url=$1
	temp_dir=/tmp/`(basename $arch_url)`
	echo $temp_dir
}

# we can afford to copy/paste since this code likely willl not change
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
        
        if [[ ! $java_ver == 1.8* ]]
        then
            echo "Required Java version 1.8, actual version $java_ver"
            echo "Install JDK 8, add it to PATH and re-run"
            exit
        fi 
        
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

# we can keep TLS-Attacker source code once the library is installed 
do_keep=0
# we can force-install, which means downloading/deploying dependencies 
# even in cases where these already exist 
force=0
if [ $# -gt 0 ]; then
	while [[ "$1" =~ ^- ]];
		do case $1 in
			-k | --keep )
				do_keep=1
				;;
			-f | --force )
				force=1
				;;
			* )
				echo "Usage: $0 [--keep|-k] [--force|-f]"
				exit 
				;;
		esac; shift; 
	done;
	if [[ $1 == '--keep' || $1 == '-k' ]] ; then 
		do_keep=1
	fi
fi

# removing leftover files/dirs from previous installations
if [ $force -eq 1 ]; then
	rm -rf `arch_temp_dir $TLSATTACKER_ARCH_URL` $TLSATTACKER_DIR
fi

# copying TLS-Attacker source code in $TLSATTACKER_DIR directory 
if [[ ! -d $TLSATTACKER_DIR ]] ; then
	
	# downloading TLS-Attacker from remote URL
	solve_arch $TLSATTACKER_ARCH_URL $TLSATTACKER_DIR
  
    # in case a patch exists for the version we rely on, we apply it before installing TLS-Attacker
	if [[ -f $TLSATTACKER_PATCH ]] ; then
		echo "Applying patch $TLSATTACKER_PATCH" 
		( 
			cd $SCRIPT_DIR 
			patch -s -p0 < $TLSATTACKER_PATCH 
		)
	fi
else
	echo "$TLSATTACKER_DIR already exists. Installing $TLSATTACKER_FULLNAME from it."
fi

# installing enhanced TLS-Attacker without running tests
echo "Installing $TLSATTACKER_FULLNAME"
( 
	cd $TLSATTACKER_DIR 
	mvn clean install -DskipTests
)

if [ $do_keep -eq 0 ]; then
	echo "Removing $TLSATTACKER_FULLNAME source code post-install"
	rm -r $TLSATTACKER_DIR
fi

# installing DTLS-Fuzzer
echo "Installing DTLS-Fuzzer"
mvn clean install
