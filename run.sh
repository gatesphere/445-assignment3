#!/usr/bin/env bash

CP=classes/.:.

echo "Running..."
if [[ $OSTYPE == linux-gnu ]] || [[ ${OSTYPE//[0-9.]/} == darwin ]]; then
  java -cp $CP $1
elif [[ $OSTYPE == cygwin ]]; then
  java -cp `cygpath -wp $CP` $1
else 
  echo "Unknown platform."
fi
echo "Done."
