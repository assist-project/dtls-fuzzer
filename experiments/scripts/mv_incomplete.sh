#!/usr/bin/env bash

fold=results/incomplete-$(date +%y%m%d%H); mkdir "${fold}"; ls -- */hyp1.dot | xargs -I{} dirname {} | xargs -I{} mv {} "${fold}"
