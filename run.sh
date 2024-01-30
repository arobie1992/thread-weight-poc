#!/bin/bash

for i in $(seq 1 100000); do
  echo "$i"
  curl http://localhost:10000 &
done