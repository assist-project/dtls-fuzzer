The folder contains so-called argument files which can be used to run the fuzzer.
To execute the fuzzer using the arguments in a file, run:
$java -jar target/dtls-fuzzer @arguments_filepath [..overwriting arguments]

The argument files are named following the pattern: [tool_name]_[library]_[configuration].
The name makes it easy to determine what will actually be executed without having to look at the content of the file.
