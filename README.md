dtls-fuzzer is a tool which performs protocol state fuzzing of DTLS servers. To that end, it supports the following functionality:
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
    > mvn clean instal

Following these steps, a target directory should have been built containing dtls-fuzzer.jar . 

# Getting Help Page
Run:
    > java -jar dtls-fuzzer.jar -help

# Learning DTLS implementations
The number of options might be overbearing. 
To learn a DTLS server implementation, one only needs to specify a few options, namely: "-connect ip_address:port" which is the address the running DTLS server is listening to.
All other options are set to default values, including the alphabet.

To just learn an existing say local server implementation listening at port 20000, run:
    > java -jar dtls-fuzzer.jar -connect localhost:20000

There will likely be issues with this type of learning. 
Learning requires that one is able to reset the server after each test.
Some servers carry some state once a test was performed, which leads to 



