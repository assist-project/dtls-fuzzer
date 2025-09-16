The folder contains so-called argument files which can be used to run the fuzzer.
To execute the fuzzer using the arguments in a file, run:
$java -jar target/dtls-fuzzer @arguments_filepath [..overwriting arguments]

The argument files are named following the pattern: [learning_method]_[tool_name]_[library]_[configuration].
The name makes it easy to determine what will actually be executed without having to look at the content of the file. Note that mealy argument files need to be run with the mealy jar, and that RA argument files need to be run with the RA jar.
