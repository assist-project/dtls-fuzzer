readonly SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
STATS_FILENAME="statistics.txt"
if [ $# = 0 ]; then
    echo "Usage: grep_stats.sh dir"
    exit
fi

models_dir=$1

function grep_stat() {
  echo $1
  for stat_file in $models_dir/*/$STATS_FILENAME; do
    grep "$2" $stat_file | grep [0-9]* -o
  done
}

function grep_timed_stat() {
  echo $1
  for stat_file in $models_dir/*/$STATS_FILENAME; do
    grep "$2" $stat_file | grep [0-9]* -o | xargs -I{} expr {} / 60000
  done
}

echo "Grep-ing stats to be introduced in the table, ensure that the results folders are ordered in the same way as table rows"
echo Implementations
ls -1 $models_dir/*/$STATS_FILENAME | xargs -i dirname {} | xargs -i basename {}
grep_stat "state count" "Number of states"
#grep_stat "hyp count" "Number of hypotheses"
#grep_stat "total tests" "Number of resets"
#grep_stat "total learning tests" "Number of learning resets"
#grep_stat "total tests to last hyp" "Number of resets up to last hyp"
#grep_timed_stat "time" "Time it took"
