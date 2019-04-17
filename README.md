TLS-ModelBasedTester is a tool which can performs automatic conformance testing of DTLS stacks using:
1. model learning to generate a model automatically
2. W-method guided conformance testing on the model, which checks that the system performs the same way when "behavior-preserving" mutations are applied

A lot of the code was imported from TLS-StateVulnFinder, which was built on Joeri's StateLearner tool.  