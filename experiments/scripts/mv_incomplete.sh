#!/usr/bin/env bash

# XXX: Take me out
# shellcheck disable=SC2011

fold=results/incomplete-$(date +%y%m%d%H); mkdir "${fold}"; ls -- */hyp1.dot | xargs -I{} dirname {} | xargs -I{} mv {} "${fold}"
