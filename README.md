TLS-ModelBasedTester is a tool which can performs automatic conformance testing of DTLS stacks using:
1. model learning to generate a model automatically
2. W-method guided conformance testing on the model, which checks that the system performs the same way when "behavior-preserving" mutations are applied.
These mutations focus primarily on fragmentation.

A lot of the code was taken/adapted from TLS-StateVulnFinder, which was built on Joeri's StateLearner tool. These tools implement learning using TLS-Attacker. 
The decision to also import the learning part, rather than to use these tools as libraries was taken so as to ensure that the setup for learning stays consistent with that for testing.