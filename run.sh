#!/usr/bin/env bash

CP=classes/.:.

echo "Running..."
if [[ $OSTYPE == solaris ]]; then
  /opt/jdk1.7.0/bin/java -cp $CP $1
elif [[ $OSTYPE == linux-gnu ]] || [[ ${OSTYPE//[0-9.]/} == darwin ]]; then
  java -cp $CP $1
elif [[ $OSTYPE == cygwin ]]; then
  java -cp `cygpath -wp $CP` $1
else 
  echo "Unknown platform."
fi
echo "Done."
