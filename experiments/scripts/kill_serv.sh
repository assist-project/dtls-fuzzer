ps | grep serv | grep -o [0-9][0-9][0-9][0-9]*  | xargs -i kill {}
