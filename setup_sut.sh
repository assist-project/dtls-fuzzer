#!/bin/bash
#
# Setup SUT which involves downloading and (where necessary) building


# the names of the suts for which directories are created should be consistent with the names appearing in argument files
readonly SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
readonly SUTS_DIR="$SCRIPT_DIR/suts"

# variable naming allows us to determine via dynamic variable resolution whether, for exempla, an SUT is fetched from a repository or from some archive
readonly MBEDTLS="mbedtls"
readonly MBEDTLS_ARCH_URL='https://tls.mbed.org/download/mbedtls-2.16.1-gpl.tgz'
readonly GNUTLS_NEW='gnutls-3.6.7'
readonly GNUTLS_NEW_ARCH_URL='ftp://ftp.gnutls.org/gcrypt/gnutls/v3.6/gnutls-3.6.7.tar.xz'
readonly GNUTLS_OLD='gnutls-3.5.19'
readonly GNUTLS_OLD_ARCH_URL='ftp://ftp.gnutls.org/gcrypt/gnutls/v3.5/gnutls-3.5.19.tar.xz'
readonly OPENSSL='openssl-1.1.1b'
readonly OPENSSL_ARCH_URL='https://www.openssl.org/source/old/1.1.1/openssl-1.1.1b.tar.gz'
readonly ETINYDTLS='etinydtls'
readonly ETINYDTLS_REP_URL='https://github.com/eclipse/tinydtls.git'
readonly ETINYDTLS_COMMIT='8414f8a'
readonly CTINYDTLS='ctinydtls'
readonly CTINYDTLS_REP_URL='https://github.com/contiki-ng/tinydtls.git'
readonly CTINYDTLS_COMMIT='53a0d97'


sutvarnames=("OPENSSL" "MBEDTLS" "ETINYDTLS" "CTINYDTLS" "GNUTLS_OLD" "GNUTLS_NEW")
sut_strings=($OPENSSL $MBEDTLS $ETINYDTLS $CTINYDTLS $GNUTLS_OLD $GNUTLS_NEW)

function get_sutvarname() {
    for varname in ${sutvarnames[*]}
    do
        sut="${!varname}"
        if [[ "$sut" = "$1" ]]; then
            echo $varname
            return 1
        fi
    done
    echo "Could not find var name for $1"
    exit -1
}

function is_rep() {
    git clone 
}

function solve_arch() {
    sut_dir=$1
    arch_url=$2
    temp_dir=/tmp/`(basename $arch_url)`
    echo $temp_dir
    echo "Fetching/unpacking from $arch_url into $sut_dir"
    if [[ ! -f "$temp_dir" ]]
    then
        echo "Downloading archive from url to $temp_dir"
        wget -nc --no-check-certificate $arch_url $temp_dir
    fi
    
    # strip components so that we $sut_dir contains the children of the archived root dir
    tar zxvf $temp_dir -C $sut_dir --strip-components=1
}

function clone_rep() {
    sut_dir=$1
    rep_url=$2
    rep_com=$3
    echo "Cloning repository $rep_url commit $rep_com to $sut_dir"
    git clone $rep_url $sut_dir
    ( cd $sut_dir ; git checkout $rep_com ; rm -rf $sut_dir/.git)
}

# Arguments: 
function download_sut() {
    sut=$1
    sut_dir=$2
    echo "Downloading files for $sut"
    # updates $sut_varname
    return_var=`get_sutvarname $sut`
    echo $return_var    
    
    # ok, is our SUT fetched from a repository?
    rep_url_var="$return_var"_REP_URL
    rep_url="${!rep_url_var}"
    if [[ -n "$rep_url" ]]; then
        rep_com_var="$return_var"_COMMIT
        rep_com="${!rep_com_var}"
        clone_rep $sut_dir $rep_url $rep_com
        repo="$sut"
    fi

    # maybe the SUT is retrieved from some archive URL
    arch_url_var="$return_var"_ARCH_URL
    arch_url="${!arch_url_var}"
    if [[ -n "$arch_url" ]]; then 
        solve_arch $sut_dir $arch_url
    fi
}


function setup_sut() {
    sut_dir=$SUTS_DIR/$1
    if [[ ! -d "$SUTS_DIR" ]]
    then
        echo "Creating SUTs directory $SUTS_DIR"
        mkdir $SUTS_DIR
    fi
    if [[ -d $sut_dir ]]
    then
        echo "SUT already exists/was already setup"
        echo "Delete $sut_dir or $SUTS_DIR to re-setup"
        return
    fi
    download_sut $1 $sut_dir 
}

if [ $# = 0 ]; then
    echo "Usage: setup_sut.sh  [SUT]..."
    echo "Where SUT is an element in: "
    for ix in ${!sut_strings[*]}
    do
        printf "   %s\n" "${sut_strings[$ix]}"
    done
    exit 1
else
    for sut in "$@"
    do 
        if [[ ! " ${sut_strings[@]} " =~ " ${sut} " ]]; then
            echo "$sut not recognized"
        else 
            setup_sut $sut
        fi
    done
fi
