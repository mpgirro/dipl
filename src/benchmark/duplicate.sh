#!/usr/bin/env bash

#for n in {001..100}; do
for n in $(seq -w 001 100); do
    #idx=$(printf "%03d" n)
    cp feeds/http___lorem-ipsum-feed.xml feeds/http___lorem-ipsum-feed-$n.xml
done
