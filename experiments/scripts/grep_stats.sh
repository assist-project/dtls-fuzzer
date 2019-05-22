echo "Grep-ing stats to be introduced in the table, ensure that the results folders are ordered in the same way as table rows"
echo "state count"
ls -1 */statistics.txt | xargs -i sh -c 'awk NR==15 {}' | grep [0-9]* -o
echo "hyp count"
ls -1 */statistics.txt | xargs -i sh -c 'awk NR==16 {}' | grep [0-9]* -o
echo "total tests"
ls -1 */statistics.txt | xargs -i sh -c 'awk NR==18 {}' | grep [0-9]* -o
echo "total learning tests"
ls -1 */statistics.txt | xargs -i sh -c 'awk NR==20 {}' | grep [0-9]* -o
echo "total tests to last hyp"
ls -1 */statistics.txt | xargs -i sh -c 'awk NR==22 {}' | grep [0-9]* -o 
echo "time"
ls -1 */statistics.txt | xargs -i sh -c 'awk NR==23 {}' | grep [0-9]* -o | xargs -I{} expr {} / 60000