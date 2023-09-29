#!/usr/bin/env bash

for f in *.png; do
    echo "Converting $f"
    convert $f -define jpeg:extent=75kb ${f%.png}.jpg
done
