ps | grep java | grep -o [0-9][0-9][0-9][0-9]* | xargs -i kill {}
