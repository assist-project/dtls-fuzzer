dtls-fuzzer is a tool which performs protocol state fuzzing of DTLS servers. To that end, it supports the following functionality:
1. given an alphabet, can automatically generate a model of a local/remote DTLS server implementation.
2. given a test (sequence of inputs) and an alphabet, can execute the test on a DTLS server implementation.

In prototype stage are the following functionalities:
1. given a model and an alphabet, performing conformance testing of a DTLS server.
2. W-method guided conformance testing on the model, which checks that the system performs the same way when "behavior-preserving" mutations are applied.
These mutations focus primarily on fragmentation.


A lot of the code was taken/adapted from TLS-StateVulnFinder, which was built on Joeri's StateLearner tool. These tools implement learning using TLS-Attacker. 
