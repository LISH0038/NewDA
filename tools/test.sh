#!/bin/bash

sleep 1 && pkill -SIGSTOP da_proc && pkill -SIGCONT da_proc && echo OK &
#./tools/stress.py -r template_java/run.sh -t perfect -l logs -p $1 -m $2
./tools/stress.py -r template_java/run.sh -t perfect -l logs -p $1 -m $2