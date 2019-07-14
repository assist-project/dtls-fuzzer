**dtls-fuzzer** is a tool which performs protocol state fuzzing of DTLS servers. To that end, it supports the following functionality:
1. given an alphabet, can automatically generate a model of a local/remote DTLS server implementation.
2. given a test (sequence of inputs) and an alphabet, can execute the test on a DTLS server implementation.
3. run a batch learning task, involving multiple 

In prototype stage are the following functionalities:
1. given a model and an alphabet, performing conformance testing of a DTLS server.
2. W-method guided conformance testing on the model, which checks that the system performs the same way when "behavior-preserving" mutations are applied.
These mutations focus primarily on fragmentation.


A lot of the code was taken/adapted from TLS-StateVulnFinder, which was built on Joeri's StateLearner tool. These tools implement learning using TLS-Attacker. 

# Installing
Run the prepare script which will deploy the two local .jars to your local repository, then install the tool itself.
Thus the on a POSIX system would be:

    > bash prepare.sh
    > mvn clean install

Following these steps, a target directory should have been built containing dtls-fuzzer.jar . 

# Getting Help Page
Run:
    > java -jar dtls-fuzzer.jar -help

# Learning DTLS implementations

The number of options might be overbearing. 
To learn a DTLS server implementation, one only needs to specify a few options, namely: "-connect ip_address:port" which is the address the running DTLS server is listening to.
All other options are set to default values, including the alphabet.

## Single learning run
To launch a learning run for an existing say local server implementation listening at port 20000, run:

    > java -jar dtls-fuzzer.jar -connect localhost:20000

There will likely be issues with this type of learning. 
Learning requires that one is able to reset the server after each test.
Some servers will carry some state from one test to another. 
This may lead to non-determinism, hence it is suggested that a command is provided by which the server can be launched.
The server will be then launched on every test, ensuring proper reset.
Example for OpenSSL:

    > java -jar dtls-fuzzer.jar -connect localhost:20000 -cmd "openssl s_server -accept 20000 -dtls1_2"
    
With so many paraments, commands can get very long. 
dtls-fuzzer uses JCommander to parse arguments, which can also read parameters from a file.
Go to 'experiments/args' for examples of arguments.
To supply an argument file to dtls-fuzzer provide it as parameter prepended by "@".
You can also add other explicit arguments to the commands (which will overwrite those in the arguments file)

    > java -jar dtls-fuzzer.jar @arg_file ...overwritting params...

## Batch learning     
To launch a batch of learning runs, one can use the 'launcher.py' script in 'experiments/scripts'. 
Provided a directory with argument files, the tool will launch a learning process for each argument file.

    > python3 launcher.py -a args_dir
    
# Running single test
To run a single test on a server using a default alphabet/one provided, you can run

    > java -jar dtls-fuzzer.jar -connect localhost:20000 -test test_file
    
For example of test files, go to 'examples/tests'. 
A test file comprises a newline separated list of inputs. 
The end of the test is either the end of the file, or an empty new line.
"#" is used to comment out a line.

If you have a model/specification, you can also run the test and compare the output with that in a specification.

    > java -jar dtls-fuzzer.jar -connect localhost:20000 -test test_file -specification model






