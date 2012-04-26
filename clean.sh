#!/usr/bin/env bash

CLASS_DIR=classes

echo "Cleaning..."

# Remove java class binaries
rm $CLASS_DIR/*

# Remove Node persistence file
rm data.bin

echo "Done."
