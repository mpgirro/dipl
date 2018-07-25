#!/usr/bin/env bash

lorem=`cat lorem-ipsum.txt`

i=0

set -- $lorem
for a; do
    shift
    for b; do
        echo $a" "$b

        i=$((i+1))
        if [[ i -eq 100 ]] ; then
            exit 0
        fi
    done
done
