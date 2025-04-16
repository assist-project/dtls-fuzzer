#!/usr/bin/env bash

if [[ $# = 0 ]]; then
    echo "usage: bash script port_num client|server|both"
    exit 1
else
    port="$1"
fi

if [[ $# = 1 ]]; then
    echo "dry run, only showing which processes would be killed"
    echo "server process info"
    ss --udp --listening -p | grep "${port}"
    echo "learner process info"
    ss --udp -p | grep "${port}"
else
    if [[ "$2" = "server" ]] || [[ "$2" = "both" ]]; then
        echo "killing server"
        ss --udp --listening -p | grep "${port}" | grep -o "pid=[0-9]*" | grep -o "[0-9]*" | xargs -I{} kill {}
    fi
    if [[ "$2" = "learner" ]] || [[ "$2" = "both" ]]; then
        echo "killing learner"
        ss --udp -p | grep "${port}" | grep -o "pid=[0-9]*" | grep -o "[0-9]*" | xargs -I{} kill {}
    fi
fi
