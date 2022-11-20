#!/usr/bin/env python3

import pydot
import sys
import os

# Inspired by https://github.com/nlhepler/pydot

if len(sys.argv) != 2 or not os.path.isfile(sys.argv[1]):
    raise Exception('Please provide the file name for the dot file.')

graph = pydot.graph_from_dot_file(sys.argv[1])
graph.set_simplify(True)
graph.write(sys.argv[1] + '.formatted.dot')

