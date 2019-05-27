echo "Removing formatted .dot files in models directory"
rm models/*formatted*dot
ls -1 models/*dot | xargs -I {} sh -c 'echo Formatting {}; python scripts/dotformat.py {}' 