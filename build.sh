#!/usr/bin/env bash

CP=.
SRC_DIR=src/
CLASS_DIR=classes/

echo "Building..."
if [ ! -d $CLASS_DIR ]; then
  mkdir $CLASS_DIR
fi

if [[ $OSTYPE == linux-gnu ]]; then
  javac -cp $CP -sourcepath $SRC_DIR -d $CLASS_DIR $1 $SRC_DIR/*.java
elif [[ $OSTYPE == cygwin ]]; then
  javac -cp `cygpath -wp $CP` -sourcepath $SRC_DIR -d $CLASS_DIR $1 $SRC_DIR/*.java
else
  echo "Unknown platform."
fi
echo "Done."