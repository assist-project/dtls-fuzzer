
#!/bin/bash
if [ $# != 2 ]; then 
   echo "bash create_args.sh results_folder args_folder"
   exit
fi

echo "Extracting arguments from results"

for result_folder in $1/*; do
   exp_name=$(basename $result_folder)
   # we extract the name of the implementation from the result folder
   args_folder=$2/args_$(echo $exp_name | cut -d_ -f1)
   mkdir -p  $args_folder
   cp $result_folder/command.args $args_folder/learn_$exp_name
done

