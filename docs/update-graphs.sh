#!/bin/sh -ex

for graph in graph graph-oauth; do
    http "http://yuml.me/diagram/scruffy/class/$(paste -s -d',' docs/$graph.yuml.txt)" > $(dirname $0)/$graph.png
done