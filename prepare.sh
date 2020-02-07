readonly TLSATTACKER_VER="3.0b"
readonly TLSATTACKER_ARCH_URL="https://github.com/RUB-NDS/TLS-Attacker/archive/$TLSATTACKER_VER.tar.gz"
readonly LIB_DIR=./lib
readonly TARGET_DIR="$LIB_DIR/TLS-Attacker-$TLSATTACKER_VER"

# installing local jars
mvn install:install-file -Dfile=$LIB_DIR/com.alexmerz.graphviz.jar -DgroupId=com.alexmerz.graphviz -DartifactId=graphviz -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=$LIB_DIR/dot-parser-0.2.jar -DgroupId=com.pfg666.dotparser -DartifactId=dot-parser -Dversion=0.2 -Dpackaging=jar -DgeneratePom=true

# downloading TLS-Attacker from remote URL
temp_arch="/tmp/TLS-Attacker-$TLSATTACKER_VER.tar.gz"
echo $temp_arch
if [[ ! -f $temp_arch ]] ; then
    echo "Downloading archive from URL to $temp_arch"
    wget -nc --no-check-certificate $TLSATTACKER_ARCH_URL -O $temp_arch
fi

if [[ ! -d $TARGET_DIR ]] ; then
    # unpacking
    mkdir $TARGET_DIR
    tar zxvf $temp_arch -C $TARGET_DIR --strip-components=1
fi

# installing enhanced TLS-Attacker without running tests
( cd $TARGET_DIR ; mvn clean install -DskipTests )