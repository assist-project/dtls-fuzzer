MODELS_DIR="."
STATS_FILENAME="statistics.txt"

function grep_stat() {
  echo $1
  for stat_file in $MODELS_DIR/*/$STATS_FILENAME; do
    grep "$2" $stat_file | grep [0-9]* -o
  done
}

function grep_timed_stat() {
  echo $1
  for stat_file in $MODELS_DIR/*/$STATS_FILENAME; do
    grep "$2" $stat_file | grep [0-9]* -o | xargs -I{} expr {} / 60000
  done
}

echo "Grep-ing stats to be introduced in the table, ensure that the results folders are ordered in the same way as table rows"
grep_stat "state count" "Number of states"
grep_stat "hyp count" "Number of hypotheses"
grep_stat "total tests" "Number of resets"
grep_stat "total learning tests" "Number of learning resets"
grep_stat "total tests to last hyp" "Number of resets up to last hyp"
grep_timed_stat "time" "Time it took"
