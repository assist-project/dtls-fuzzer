fold=results/incomplete-`date +%y%m%d%H`; mkdir $fold; ls */hyp1.dot | xargs -i dirname {} | xargs -i mv {} $fold
