#!/usr/bin/env bash

# XXX: Take me out
# shellcheck disable=SC2011

STATS_FILENAME="statistics.txt"
if [[ $# = 0 ]]; then
    echo "Usage: collect_stats.sh dir"
    echo ""
    echo "dir - directory containing output folders resulting from learning experiments"
    exit
fi

models_dir=$1

function grep_stat() {
  echo "$1"
  for stat_file in "${models_dir}"/*/"${STATS_FILENAME}"; do
    grep "$2" "${stat_file}" | grep "[0-9]*" -o
  done
}

function grep_timed_stat() {
  echo "$1"
  for stat_file in "${models_dir}"/*/"${STATS_FILENAME}"; do
    grep "$2" "${stat_file}" | grep "[0-9]*" -o | xargs -I{} expr {} / 60000
  done
}

echo "Grep-ing stats to be directory ${models_dir}"
echo Implementations

ls -1 "${models_dir}"/*/"${STATS_FILENAME}" | xargs -I{} dirname {} | xargs -I{} basename {}
grep_stat "state count" "Number of states"
grep_stat "hyp count" "Number of hypotheses"
grep_stat "total tests" "Number of tests"
grep_stat "total learning tests" "Number of learning tests"
grep_stat "total tests to last hyp" "Number of tests up to last hyp"
grep_timed_stat "time" "Time it took"
