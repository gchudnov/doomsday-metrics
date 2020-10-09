#!/usr/bin/env bash

# gets CPU information from `top`

top -b -n5 -d.2 | grep "Cpu" | tail -n1 | awk '{print($2)}' | cut -d'%' -f 1
