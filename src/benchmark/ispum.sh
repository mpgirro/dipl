#!/usr/bin/env bash

if [[ -z $1 ]]
then
    echo "Usage: $0 <count>"
    exit 1
fi

for n in $(seq -w 001 $1); do
    echo "lorem ipsum $n" >> queries-lorem$1.txt
done
