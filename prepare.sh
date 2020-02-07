LIB_DIR=./lib
mvn install:install-file -Dfile=$LIB_DIR/com.alexmerz.graphviz.jar -DgroupId=com.alexmerz.graphviz -DartifactId=graphviz -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=$LIB_DIR/dot-parser-0.2.jar -DgroupId=com.pfg666.dotparser -DartifactId=dot-parser -Dversion=0.2 -Dpackaging=jar -DgeneratePom=true
# installing enhanced TLS-Attacker
( cd $LIB_DIR/TLS-Attacker-Sources ; mvn install -DskipTests )