#!/usr/bin/env python3

import pydot
import sys
import os

# Inspired by https://github.com/nlhepler/pydot
fout = ""
if len(sys.argv) not in [2,3]  or not os.path.isfile(sys.argv[1]):
   raise Exception('Please provide the file name for the dot file and optionally the output file name.')
elif len(sys.argv) == 3:
   fout = sys.argv[2]
else:
   fout = sys.argv[1].replace('.dot','') + '.formatted.dot' 
graph = pydot.graph_from_dot_file(sys.argv[1]) 
graph.set_simplify(True)
graph.write(fout)

