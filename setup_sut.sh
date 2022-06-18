#!/usr/bin/env bash
#
# Setup SUT which may involve downloading, patching, installing dependencies and building
# The goal is to have a runnable SUT

# SCRIPT_DIR should correpond to dtls-fuzzer's root directory
readonly SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
# the (temporary) directory storing all the archived sources that were downloaded
readonly DOWNLOAD_DIR="/tmp/dtls-fuzzer"
# dir where the suts are stored
readonly SUTS_DIR="$SCRIPT_DIR/suts"
# where binaries are located
readonly BIN_DIR="$SCRIPT_DIR/bin"
# dir where precomplied suts are stored
readonly SUT_JAR_DIR="$SCRIPT_DIR/experiments/suts"
# patches
readonly PATCHES_DIR="$SCRIPT_DIR/experiments/patches"
# source folders (similar to patches, contain files to replace certain files in the SUT folder with )
readonly SOURCES_DIR="$SCRIPT_DIR/experiments/sources"
# dir where the modules SUTs rely on are stored
readonly MODULES_DIR="$SCRIPT_DIR/modules"

# the names of the suts for which directories are created should be consistent with the names appearing in argument files
# variable naming allows us to determine via dynamic variable resolution whether, for example, an SUT is fetched from a repository or from some archive
# for each implementation we maintain a set of variables that are not tied to a version for quick modification
readonly MBEDTLS="mbedtls-2.16.1"
readonly MBEDTLS_ARCH_URL='https://tls.mbed.org/download/mbedtls-2.16.1-gpl.tgz'
readonly MBEDTLS_2250="mbedtls-2.25.0"
readonly MBEDTLS_2250_ARCH_URL='https://github.com/ARMmbed/mbedtls/archive/mbedtls-2.25.0.tar.gz'
readonly MBEDTLS_2260="mbedtls-2.26.0"
readonly MBEDTLS_2260_ARCH_URL='https://github.com/ARMmbed/mbedtls/archive/mbedtls-2.26.0.tar.gz'

readonly GNUTLS_LATEST='gnutls-latest'
readonly GNUTLS_LATEST_REP_URL='https://gitlab.com/gnutls/gnutls.git'
readonly GNUTLS_371="gnutls-3.7.1"
readonly GNUTLS_371_ARCH_URL='https://www.gnupg.org/ftp/gcrypt/gnutls/v3.7/gnutls-3.7.1.tar.xz'
readonly GNUTLS_367='gnutls-3.6.7'
readonly GNUTLS_367_ARCH_URL='ftp://ftp.gnutls.org/gcrypt/gnutls/v3.6/gnutls-3.6.7.tar.xz'
readonly GNUTLS_3519='gnutls-3.5.19'
readonly GNUTLS_3519_ARCH_URL='ftp://ftp.gnutls.org/gcrypt/gnutls/v3.5/gnutls-3.5.19.tar.xz'

readonly OPENSSL='openssl-1.1.1b'
readonly OPENSSL_ARCH_URL='https://www.openssl.org/source/old/1.1.1/openssl-1.1.1b.tar.gz'
readonly OPENSSL_111g='openssl-1.1.1g'
readonly OPENSSL_111g_ARCH_URL='https://www.openssl.org/source/old/1.1.1/openssl-1.1.1g.tar.gz'
readonly OPENSSL_111k='openssl-1.1.1k'
readonly OPENSSL_111k_ARCH_URL='https://www.openssl.org/source/openssl-1.1.1k.tar.gz' #this will not be valid for much longer

readonly ETINYDTLS='etinydtls'
readonly ETINYDTLS_REP_URL='https://github.com/eclipse/tinydtls.git'
readonly ETINYDTLS_COMMIT='8414f8a'
readonly ETINYDTLS_DEVELOP="etinydtls-develop"
readonly ETINYDTLS_DEVELOP_REP_URL='https://github.com/eclipse/tinydtls.git'
readonly ETINYDTLS_DEVELOP_COMMIT='19d9fcf'


#readonly ETINYDTLS_ARCH_URL='https://github.com/eclipse/tinydtls/archive/706888256c3e03d9fcf1ec37bb1dd6499213be3c.tar.gz'
readonly CTINYDTLS='ctinydtls'
readonly CTINYDTLS_REP_URL='https://github.com/contiki-ng/tinydtls.git'
readonly CTINYDTLS_COMMIT='53a0d97'
#readonly CTINYDTLS_ARCH_URL='https://github.com/contiki-ng/tinydtls/archive/53a0d97da748a67093c49cb38744650c71d58c4d.tar.gz'

readonly SCANDIUM_OLD='scandium-2.0.0-M16'
readonly SCANDIUM_OLD_ARCH_URL='https://github.com/assist-project/scandium-dtls-examples/archive/v2.0.0-M16.tar.gz'
readonly CALIFORNIUM_OLD="californium-2.0.0-M16"
readonly CALIFORNIUM_OLD_ARCH_URL='https://github.com/eclipse/californium/archive/2.0.0-M16.tar.gz'
readonly SCANDIUM_230="scandium-2.3.0"
readonly SCANDIUM_230_ARCH_URL='https://github.com/assist-project/scandium-dtls-examples/archive/v2.3.0.tar.gz'
readonly CALIFORNIUM_230="californium-2.3.0"
readonly CALIFORNIUM_230_ARCH_URL='https://github.com/eclipse/californium/archive/2.3.0.tar.gz'
readonly SCANDIUM_262="scandium-2.6.2"
readonly SCANDIUM_262_ARCH_URL='https://github.com/assist-project/scandium-dtls-examples/archive/v2.6.2.tar.gz'
readonly CALIFORNIUM_262="californium-2.6.2"
readonly CALIFORNIUM_262_ARCH_URL='https://github.com/eclipse/californium/archive/2.6.2.tar.gz'
readonly SCANDIUM_300_M2="scandium-3.0.0-M2"
readonly SCANDIUM_300_M2_ARCH_URL='https://github.com/assist-project/scandium-dtls-examples/archive/v3.0.0-M2.tar.gz'
readonly CALIFORNIUM_300_M2="californium-3.0.0-M2"
readonly CALIFORNIUM_300_M2_ARCH_URL='https://github.com/eclipse/californium/archive/refs/tags/3.0.0-M2.tar.gz'

readonly WOLFSSL="wolfssl-4.0.0"
readonly WOLFSSL_ARCH_URL='https://github.com/wolfSSL/wolfssl/archive/v4.0.0-stable.tar.gz'
readonly WOLFSSL_440="wolfssl-4.4.0"
readonly WOLFSSL_440_ARCH_URL='https://github.com/wolfSSL/wolfssl/archive/v4.4.0-stable.tar.gz'
readonly WOLFSSL_471r="wolfssl-4.7.1r"
readonly WOLFSSL_471r_ARCH_URL='https://github.com/wolfSSL/wolfssl/archive/refs/tags/v4.7.1r.tar.gz'

readonly PIONDTLS_USENIX="piondtls-usenix" # the usenix version sits one commit before 1.5.2 (the commit after it fixes the application processing bug)
readonly PIONDTLS_USENIX_REP_COMMIT="e4481fc"
readonly PIONDTLS_USENIX_ARCH_URL="https://github.com/assist-project/pion-dtls-examples/archive/v1.5.2.tar.gz"
readonly PIONDTLS_152="piondtls-1.5.2" # likely works with other v1.x.x versions
readonly PIONDTLS_152_ARCH_URL="https://github.com/assist-project/pion-dtls-examples/archive/v1.5.2.tar.gz"
readonly PIONDTLS_202="piondtls-2.0.2" # likely works with other v2.x.x versions
readonly PIONDTLS_202_ARCH_URL="https://github.com/assist-project/pion-dtls-examples/archive/v2.0.2.tar.gz"
readonly PIONDTLS_209="piondtls-2.0.9" # likely works with other v2.x.x versions
readonly PIONDTLS_209_ARCH_URL="https://github.com/assist-project/pion-dtls-examples/archive/refs/tags/v2.0.9.tar.gz"


#readonly JSSE_ARCH_URL="https://github.com/assist-project/jsse-dtls-clientserver/archive/refs/tags/v1.0.0.tar.gz"
readonly JSSE_LOCAL=$SOURCES_DIR/jsse-dtls-clientserver
readonly JSSE_904="jsse-9.0.4"
readonly JSSE_904_LOCAL=$JSSE_LOCAL
#readonly JSSE_904_ARCH_URL=$JSSE_ARCH_URL
readonly JSSE_11010="jsse-11.0.10"
readonly JSSE_11010_LOCAL=$JSSE_LOCAL
#readonly JSSE_11010_ARCH_URL=$JSSE_ARCH_URL
readonly JSSE_1202="jsse-12.0.2"
readonly JSSE_1202_LOCAL=$JSSE_LOCAL
#readonly JSSE_1202_ARCH_URL=$JSSE_ARCH_URL
readonly JSSE_1302="jsse-13.0.2"
readonly JSSE_1302_LOCAL=$JSSE_LOCAL
#readonly JSSE_1302_ARCH_URL=$JSSE_ARCH_URL
readonly JSSE_1501="jsse-15.0.1"
readonly JSSE_1501_LOCAL=$JSSE_LOCAL
#readonly JSSE_1501_ARCH_URL=$JSSE_ARCH_URL
readonly JSSE_1601="jsse-16.0.1"
readonly JSSE_1601_LOCAL=$JSSE_LOCAL
#readonly JSSE_1601_ARCH_URL=$JSSE_ARCH_URL


# dependencies
readonly JDK_904="jdk-9.0.4"
readonly JDK_904_URL="https://download.java.net/java/GA/jdk9/9.0.4/binaries/openjdk-9.0.4_linux-x64_bin.tar.gz"
readonly JDK_11010="jdk-11.0.10"
readonly JDK_11010_URL="https://github.com/AdoptOpenJDK/openjdk11-upstream-binaries/releases/download/jdk-11.0.10%2B9/OpenJDK11U-jdk_x64_linux_11.0.10_9.tar.gz"
readonly JDK_1202="jdk-12.0.2"
readonly JDK_1202_URL="https://download.java.net/java/GA/jdk12.0.2/e482c34c86bd4bf8b56c0b35558996b9/10/GPL/openjdk-12.0.2_linux-x64_bin.tar.gz"
readonly JDK_1302="jdk-13.0.2"
readonly JDK_1302_URL="https://download.java.net/java/GA/jdk13.0.2/d4173c853231432d94f001e99d882ca7/8/GPL/openjdk-13.0.2_linux-x64_bin.tar.gz"
readonly JDK_1501="jdk-15.0.1"
readonly JDK_1501_URL="https://download.java.net/java/GA/jdk15.0.1/51f4f36ad4ef43e39d0dfdbaf6549e32/9/GPL/openjdk-15.0.1_linux-x64_bin.tar.gz"
readonly JDK_1601="jdk-16.0.1"
readonly JDK_1601_URL="https://download.java.net/java/GA/jdk16.0.1/7147401fd7354114ac51ef3e1328291f/9/GPL/openjdk-16.0.1_linux-x64_bin.tar.gz"
readonly NETTLE_36_ARCH_URL="https://ftp.gnu.org/gnu/nettle/nettle-3.6.tar.gz"
readonly NETTLE_36="nettle-3.6"
readonly NETTLE_341_ARCH_URL="https://ftp.gnu.org/gnu/nettle/nettle-3.4.1.tar.gz"
readonly NETTLE_341="nettle-3.4.1"
readonly AUTOCONF_ARCH_URL="https://ftp.gnu.org/gnu/autoconf/autoconf-2.69.tar.gz"
readonly AUTOCONF="autoconf-2.69"
readonly M4_ARCH_URL="https://ftp.gnu.org/gnu/m4/m4-1.4.18.tar.gz"
readonly M4="m4-1.4.18"
readonly LIBTOOL_ARCH_URL="http://ftpmirror.gnu.org/libtool/libtool-2.4.6.tar.gz"
readonly LIBTOOL="libtool-2.4.6"

sutvarnames=("OPENSSL" "OPENSSL_111g" "OPENSSL_111k"\ 
"MBEDTLS" "MBEDTLS_2250" "MBEDTLS_2260"\ 
"ETINYDTLS" "ETINYDTLS_DEVELOP" "CTINYDTLS"\ 
"GNUTLS_3519" "GNUTLS_367" "GNUTLS_371" "GNUTLS_LATEST"\ 
"SCANDIUM_OLD" "SCANDIUM_230" "SCANDIUM_262" "SCANDIUM_300_M2"\ 
"JSSE_904" "JSSE_11010" "JSSE_1202" "JSSE_1302" "JSSE_1501" "JSSE_1601"\ 
"WOLFSSL" "WOLFSSL_440 WOLFSSL_471r"\ 
"PIONDTLS_USENIX" "PIONDTLS_152" "PIONDTLS_202" "PIONDTLS_209")

sut_strings=($OPENSSL $OPENSSL_111g $OPENSSL_111k\ 
$MBEDTLS $MBEDTLS_2250 $MBEDTLS_2260\ 
$ETINYDTLS $ETINYDTLS_DEVELOP $CTINYDTLS\ 
$GNUTLS_3519 $GNUTLS_367 $GNUTLS_371 $GNUTLS_LATEST\ 
$SCANDIUM_OLD $SCANDIUM_230 $SCANDIUM_262 $SCANDIUM_300_M2\ 
$JSSE_904 $JSSE_11010 $JSSE_1202 $JSSE_1302 $JSSE_1501 $JSSE_1601\ 
$WOLFSSL $WOLFSSL_440 $WOLFSSL_471r\ 
$PIONDTLS_USENIX $PIONDTLS_152 $PIONDTLS_202 $PIONDTLS_209)


# options for when setting up SUT
opt_no_patch=0
opt_create_patch=0
opt_no_dep=0
opt_no_build=0
opt_debug_build=0
opt_force=0

# works for most SUTs
function get_ver() {
    sut=$1
    echo ${sut##*-}
}

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

function get_jar_path() {
    sut=$1
    return_var=`get_sutvarname $sut`

    # ok, is our SUT a pre-packaged -jar 
    jar_path_var="$return_var"_JAR_PATH
    jar_path="${!jar_path_var}"
    echo $jar_path
}

function get_rep_url() {
    sut=$1
    return_var=`get_sutvarname $sut`
    
    # ok, is our SUT fetched from a repository?
    rep_url_var="$return_var"_REP_URL
    rep_url="${!rep_url_var}"
    echo $rep_url
}


# Returns the expected varname for the given library name
function lib_to_varname() {
    name=$1

    dotstripped=${name//\./}
    hyphenrepl=`echo $dotstripped | sed -e 's/-/_/g'`
    toupper=${hyphenrepl^^}
    echo $toupper
}

# ./configure --with-guile-site-dir=no --prefix=/bin/ --with-included-libtasn1 --with-included-unistring --without-p11-kit --disable-guile --disable-doc
function solve_arch() {
    arch_url=$1
    target_dir=$2
    if [[ ! -d $DOWNLOAD_DIR ]]
    then
        echo "Creating folder $DOWNLOAD_DIR for storing code downloaded from the Internet"
        mkdir $DOWNLOAD_DIR
    fi
    arch_file=$DOWNLOAD_DIR/`(basename $arch_url)`
    echo $arch_file
    echo "Fetching/unpacking from $arch_url into $target_dir"
    if [[ ! -f "$arch_file" ]]
    then
        echo "Downloading archive from url to $arch_file"
        wget -nc --no-check-certificate $arch_url -O $arch_file
    fi

    if [[ ! -s $arch_file ]]; then
        echo "Failed to load the archive at the URL: $arch_url"
        rm $arch_file
        exit 1
    fi
    
    mkdir $target_dir
    # ${arch_file##*.} retrieves the substring between the last index of . and the end of $arch_file
    arch=`echo "${arch_file##*.}"`
    if [[ $arch == "xz" ]]
    then
        tar_param="-xJf"
    else 
        tar_param="zxvf"
    fi
    
    if [ $target_dir ] ; then
        tar $tar_param $arch_file -C $target_dir --strip-components=1
    else 
        tar $tar_param $arch_file
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

# Downloads the SUT source files and places them in a SUT directory 
function download_sut() {
    sut=$1
    sut_dir=$2

    echo "Downloading files for $sut"
    # updates $sut_varname
    return_var=`get_sutvarname $sut`

    # ok, is our SUT fetched from a repository?
    rep_url_var="$return_var"_REP_URL
    rep_url="${!rep_url_var}"
    if [[ -n "$rep_url" ]]; then
        rep_com_var="$return_var"_COMMIT
        rep_com="${!rep_com_var}"
        clone_rep $sut_dir $rep_url $rep_com
        repo="$sut"
        return 
    fi

    # maybe the SUT is retrieved from a URL pointing to some archive
    arch_url_var="$return_var"_ARCH_URL
    arch_url="${!arch_url_var}"
    if [[ -n "$arch_url" ]]; then 
        solve_arch $arch_url $sut_dir
        return 
    fi

    # maybe the SUT is stored locally, in which case we only have to copy/paste the sources
    local_dir_var="$return_var"_LOCAL
    local_dir="${!local_dir_var}"
    if [[ -n "$local_dir" ]]; then
        cp -r $local_dir $sut_dir
        return 
    fi
    
    
}

# Applies patches for SUTs that require them
function apply_patch() {
    sut=$1
    sut_dir=$2
    #$(echo $sut | cut -d '-' -f 1)
    sut_patch=$PATCHES_DIR/$sut.patch
    
    if [[ -f $sut_patch ]] 
    then
        echo "Applying patch $sut_patch"
        rep_url=`get_rep_url $sut`
        if [[ -n "$rep_url" ]]; then
            echo "via git apply"
            ( cd $sut_dir; git apply $sut_patch )
        else 
            echo "via patch"
            patch -s -p0 < $sut_patch
        fi
    fi
}

# Replaces files in the SUT by files in the sources directory at the same path
function replace_sut_files() {
    sut=$1
    sut_dir=$2
    sut_sources=$SOURCES_DIR/$sut

    if [[ -d $sut_sources ]]
    then
        echo "Replacing source files in $sut"
        echo "rsync -a -v -h --progress $sut_sources $SUTS_DIR"
        rsync -a -v -h --progress $sut_sources $SUTS_DIR
    fi
}

# Makes bin directory if it doesn't exist
function make_bin() {
    if [[ ! -d $BIN_DIR ]]; then
        mkdir $BIN_DIR
    fi
}

# Generic dependency install function
function install_dep() {
    dep_dir="$MODULES_DIR/$1"
    dep_url=$2

    if [[ ! -d "$dep_dir" ]]; then
        solve_arch $dep_url $dep_dir
        if [[ -f $dep_dir/configure ]]; then 
            ( cd $dep_dir ; ./configure ; sudo make install )
        elif [[ -f $dep_dir/pom.xml ]]; then
            ( cd $dep_dir ; mvn install -DskipTests )
        fi
    fi
}

# Gets nettle library
function get_nettle() {
    sut=$1
 
    if [[ $sut == $GNUTLS_371 || $sut == $GNUTLS_LATEST ]]; then
        echo $NETTLE_36
    else 
        echo $NETTLE_341
    fi
}

# Gets download url for library
function get_arch_url() {
    lib=$1

    varname=`lib_to_varname $lib`
    if [[ -z $varname ]]; then
        echo "There is no variable with name $varname associated with lib $lib"
        exit 1
    fi

    archurl_varname=$varname"_ARCH_URL"
    archurl="${!archurl_varname}"
    echo $archurl
}

# Downloads and installs all the libraries the SUT depends on
function install_sut_dep() {
    sut=$1

    if [[ ! -d $MODULES_DIR ]]; then
        mkdir $MODULES_DIR
    fi

    # JSSE SUT, meaning all we need to ensure is that the right vm is installed
    if [[ $sut == jsse* ]]; then
        ver=`get_ver $sut`
        verstripped=${ver//\./}
        jdk_varname="JDK_$verstripped"
        jdk_name="${!jdk_varname}"

        if [[ -z $jdk_name ]]; then
            echo "Variable $jdk_varname expected but not defined"
            exit 1
        fi

        jdk_urlvarname=$jdk_varname"_URL"
        jdk_url="${!jdk_urlvarname}"
        if [[ -z $jdk_url ]]; then
            echo "Variable $jdk_url expected but not defined"
            exit 1
        fi

        install_dep $jdk_name $jdk_url
    elif [[ $sut == pion* ]]; then
        if [[ $sut == *usenix* ]]; then 
            go get github.com/pion/dtls@$PIONDTLS_USENIX_REP_COMMIT
        else 
            ver=`get_ver $sut`
            if [[ $sut == *piondtls-1* ]]; then
                go get github.com/pion/dtls@v$ver
            elif [[ $sut == *piondtls-2* ]]; then 
                go get github.com/pion/dtls/v2@v$ver
            fi
        fi
    elif [[ $sut == gnutls* ]]; then
        install_dep $M4 $M4_ARCH_URL
        sudo apt-get install pkg-config
        nettle=`get_nettle $sut`
        nettle_url=`get_arch_url $nettle`
        install_dep $nettle $nettle_url
    elif [[ $sut == etinydtls* ]]; then
        install_dep $M4 $M4_ARCH_URL
        install_dep $AUTOCONF $AUTOCONF_ARCH_URL  
    elif [[ $sut == wolfssl* ]]; then
	    install_dep $M4 $M4_ARCH_URL
        install_dep $AUTOCONF $AUTOCONF_ARCH_URL
	    install_dep $LIBTOOL $LIBTOOL_ARCH_URL
    elif [[ $sut == scandium* ]]; then 
        if [[ $sut == $SCANDIUM_OLD ]]; then
            install_dep $CALIFORNIUM_OLD $CALIFORNIUM_OLD_ARCH_URL
        elif [[ $sut == $SCANDIUM_230 ]]; then
            install_dep $CALIFORNIUM_230 $CALIFORNIUM_230_ARCH_URL
        elif [[ $sut == $SCANDIUM_262 ]]; then
            install_dep $CALIFORNIUM_262 $CALIFORNIUM_262_ARCH_URL
        elif [[ $sut == $SCANDIUM_300_M2 ]]; then
            install_dep $CALIFORNIUM_300_M2 $CALIFORNIUM_300_M2_ARCH_URL
        fi
    fi
}

# Builds the SUT. In this process also installs/deploys all necessary dependencies
function make_sut() {
    sut=$1
    sut_dir=$2

    if [[ $sut == jsse* ]]; then
        ver=`get_ver $sut`
        (cd $sut_dir; JAVA_HOME=$MODULES_DIR/jdk-$ver mvn install assembly:single; cp target/jsse-dtls-clientserver.jar ../jsse-$ver-dtls-clientserver.jar)
        return 0
    elif [[ $sut == pion* ]]; then
        ( cd $sut_dir; go build -o dtls-clientserver main/main.go )
        return 0
    elif [[ $sut == gnutls* ]]; then
        ( 
            cd $sut_dir  
            if [[ ! -f ./configure ]]; then 
                ./bootstrap
            fi
            PKG_CONFIG_PATH="$MODULES_DIR/$nettle:$PKG_CONFIG_PATH" ./configure --enable-static --with-guile-site-dir=no --with-included-libtasn1 --with-included-unistring --without-p11-kit --disable-guile --disable-doc
        )
    elif [[ $sut == etinydtls* ]]; then
        ( cd $sut_dir ; autoconf ; autoheader ; ./configure )
    elif [[ $sut == openssl* ]]; then
        if [[ $opt_debug_build -eq 1 ]]; then
            ( cd $sut_dir ; ./config -d -static )
        else 
            ( cd $sut_dir ; ./config -static )  
        fi
    elif [[ $sut == wolfssl* ]]; then
        # this configuration is for PSK wolfssl
        ( cd $sut_dir ; bash autogen.sh ; AM_CFLAGS='-DHAVE_AES_CBC -DWOLFSSL_AES_128' ./configure --enable-dtls --enable-psk --enable-rsa --enable-sha --enable-debug C_EXTRA_FLAGS=-DWOLFSSL_STATIC_PSK )
    elif [[ $sut == scandium* ]]; then
        for sc_prog in $sut_dir/sc*; do
            ( cd $sc_prog; mvn install; cp target/scandium*jar $SUTS_DIR )
        done
    fi

    make_path="$sut_dir/Makefile"
    if [[ -f "$make_path" ]]; then
        echo "Running make inside $sut_dir"
        ( cd $sut_dir; make )
        # tinydtls exceptions
        if [[ $sut == *tinydtls* ]]; then
            test_dir="$sut_dir/tests"
            ( cd $test_dir; make)
        fi
    fi
}

function setup_sut() {
    sut=$1

    # initialization
    sut_dir=$SUTS_DIR/$sut

    if [[ ! -d "$SUTS_DIR" ]]
    then
        echo "Creating SUTs directory $SUTS_DIR"
        mkdir $SUTS_DIR
    fi
    if [[ -d $sut_dir ]]
    then
        echo "SUT already exists/was already setup"
        if [[ opt_force -eq 0 ]]; then
            echo "Delete $sut_dir or $SUTS_DIR to re-setup, or use --force option"
            return
        else 
            echo "Removing existing SUT"
            rm -r $sut_dir
        fi
    fi

    # download the SUT sources
    download_sut $sut $sut_dir 

    if [[ $opt_no_patch -eq 0 ]]; then
    
        if [[ ! $opt_create_patch -eq 0 ]]; then
            sut_orig_dir=$sut_dir"_orig"
            cp -r $sut_dir $sut_orig_dir
        fi

        # apply the patch if a patch for the SUT has been developed
        apply_patch $sut $sut_dir
        # replace source files in SUT with adapted ones, if a source replacement directory exists for the SUL
        replace_sut_files $sut $sut_dir

        if [[ ! $opt_create_patch -eq 0 ]]; then
            diff -ruN $sut_orig_dir $sut_dir > $SUTS_DIR/$sut".patch"
            rm -r $sut_orig_dir
        fi
    fi

    if [[ $opt_no_dep -eq 0 ]]; then
        #install SUT dependencies
        install_sut_dep $sut
    fi

    if [[ $opt_no_build -eq 0 ]]; then 
        # build the SUT
        make_sut $sut $sut_dir
    fi
}

if [[ -z $USE_AS_LIBRARY ]]; then 
    if [ $# = 0 ]; then
        echo "Usage: setup_sut.sh  [options] [SUT]..."
        echo "Where SUT is an element in: "
        for ix in ${!sut_strings[*]}
        do
            printf "   %s\n" "${sut_strings[$ix]}"
        done
        echo ""
        echo "Supported options: --no-patch, --no-build, --no-dep, --force"
        echo "--no-patch|-np     do not apply patch on the SUT"
        echo "--create-patch|-cp     create a patch which shows the adaptations made to the SUT"
        echo "--no-build|-nb     do not build the SUT"
        echo "--no-dep|nd     do not download/install dependencies"
        echo "--debug-build|-db  build with debug symbols (where supported)"
        echo "--force|-f     if SUT folder exists, delete it and rebuild the SUT instead of generating a message"
        echo ""
        echo "Archived SUT/dependency source code downloaded from the Internet is stored in $DOWNLOAD_DIR, which serves as a cache."
        echo "Delete this folder or archives in this folder in case the local source code is not up-to-date with the remote code."
        exit 1
    else
        while [[ "$1" =~ ^- ]]; do case $1 in
        -nb | --no-build )
            opt_no_build=1
            ;;
        -db | --debug-build )
            opt_debug_build=1
            ;;
        -np | --no-patch )
            opt_no_patch=1
            ;;
        -nd | --no-dep )
            opt_no_dep=1
            ;;
        -cp | --create-patch )
            opt_create_patch=1
            ;;
        -f | --force )
            opt_force=1
            ;;
        * )
            echo "Unsupported option $1"
            return
            ;;
        esac; shift; done
        for sut in "$@"
        do 
            if [[ ! " ${sut_strings[@]} " =~ " ${sut} " ]]; then
                echo "$sut not recognized"
            else 
                setup_sut $sut
            fi
        done
    fi
fi
