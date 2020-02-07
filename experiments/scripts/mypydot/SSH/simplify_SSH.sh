#!/bin/sh
for file in "$@"
do
    simFile="sim"$file
    echo "$simFile"
    cp $file $simFile
    sed -i 's/\[long\,\ *long\]//g' $simFile
    sed -i 's/?CLOSE\[\]/CLOSE/g' $simFile
    sed -i 's/TIMEOUT\[\]/TIMEOUT/g' $simFile
    sed -i 's/Connect\[\]/CONNECT/g' $simFile
    sed -i 's/\!O//g' $simFile
    sed -i 's/\?I//g' $simFile
    sed -i 's/\s*\&amp\;\&amp\;\s*TRUE//g' $simFile
    sed -i 's/TRUE\s*\&amp\;\&amp\;\s*//g' $simFile
    sed -i 's/[\(\)]//g' $simFile
    sed -i 's/>\]/"]/g' $simFile
    sed -i 's/\=</="/g' $simFile
#    sed -i 's/>\[/>{/g' $simFile
#    sed -i 's/\]\"/}"/g' $simFile
    sed -i 's/\[.ong\]//g' $simFile
    sed -iEn "s/\"l\([^\"]*\)\"/l\1/g" $simFile
#    sed -iEn "s/\"/l/g" $simFile
    sed -i 's/<BR\s\/>/ /g' $simFile
    sed -i 's/shape\=doublecircle\s,style\=dotted/shape=point, style=solid'/g $simFile
    sed -i 's/shape\=doublecircle\s,style\=solid/shape=ellipse, style=solid'/g $simFile
#    sed -i 's/{/paral/g' $simFile
#    sed -i 's/}/parar/g' $simFile
done
