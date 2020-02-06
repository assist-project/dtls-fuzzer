**dtls-fuzzer** is a Java tool which performs protocol state fuzzing of DTLS servers. To that end, it supports the following functionality:
1. given an alphabet, can automatically generate a model of a local/remote DTLS server implementation.
2. given a test (sequence of inputs) and an alphabet, can execute the test on a DTLS server implementation.
3. run a batch learning task, involving multiple 

**dtls-fuzzer** uses **TLS-Attacker** to generate/parse DTLS messages as well as to maintain state. 
To that end, as part of the work, **TLS-Attacker** has been extended with support for DTLS.
The extension is available in the version 3.0 of **TLS-Attacker**.

# Artifact contents
The artifact contains:
1. a description of the file structure of **dtls-fuzzer**, including source code and experimental data consistent with that displayed in the paper.
2. a walkthrough for evaluating **dtls-fuzzer** on a chosen SUT (System Under Test)/ DTLS server implementation. 

# dtls-fuzzer file structure
The most important folders in **dtls-fuzzer**'s root directory are:
1. 'src', directory containing the Java source code of **dtls-fuzzer**. 
2. 'examples', directory containing examples of alphabets, tests, specifications (i.e. models) and example of arguments that can be supplied to **dtls-fuzzer**, to launch learning experiments. Files here are used as inputs for learning experiments.
3. 'experiments', directory containing data pertaining to experiments. Some of this data also serves as input during learning experiments. The most notable folders are:
    1. 'suts', with binaries for Java SUTs. These SUTs are custom-made DTLS server programs whose source code is publically available. 
    2. 'patches', patches that were applied to some SUTs (particularly to utilities) before the source code was compiled. The primary purpose of these patches was to prevent timing induced-behavior during learning, enable/disable functionality in the SUT, and configure parameters such as the pre-shared key.
    4. 'keystore', key material (e.g. public-private key pairs, Java keystores) used during learning
    5. 'results', experimental results

## Experimental results
'experiments/results' contains experimental results, which are the main output of the work. In particular
- 'all_ciphers' contains output folders for all the experiments run
    - 'mapper' contains experimental results which help justify some of the mapper decisions made (see Section 5.2)
- 'included' contains output folders for converging experiments (converging means that learning successfully generates a model). Note that not all experiments in 'all_ciphers' converged
- 'archive' contains previous experiments not considered in the work

Output folders are named based on the experiment configuration, that is: 
- the SUT/implementation tested, 
- the alphabet used, 
- where applicable whether client certification was required (req), optional (nreq) or disabled (none), 
- the testing algorithm: random walk (rwalk) or an adaptation of it (stests). Experiments using the adaptation have not been included in the paper.
- optionally, whether retransmissions were included (incl or excl). Retransmissions were included by default. 

An output folder contains:
- 'alphabet.xml', the alphabet 
- 'command.args', the arguments file used
- 'sul.config', SUT-dependant configuration for **TLS-Attacker**,  the same configuration could be used to execute workflow traces on the SUT using **TLS-Attacker** alone
- 'hyp[0-9]+.dot', intermediate hypotheses 
- 'statistics.txt', experiment statistics such as the total number of tests, learning time. Table 4 displays this data
- 'nondet.log', logs of encountered non-deterministic behavior
- 'learnedModel.dot', in case learning converged, the learned model (or final hypothesis)
- 'error.msg', an error message generated in case the experiment failed/learning was stopped and hence, did not converge to a final model. The main culprit is non-determinism.

The evaluator can check (for example) that experimental results in 'included' correspond to those displayed in Table 4, or that configurations tested in Table 2 also appear in 'all_ciphers'.
Note that models appearing in the paper were the result of significant pruning. 
See more here **TODO**. 

# dtls-fuzzer evaluation steps
For the purpose of evaluating **dtls-fuzzer** it is necessary to perform the following steps:
1. Ensure pre-requisites are met
2. Install dtls-fuzzer
3. Setup SUT 
4. Use dtls-fuzzer to generate models for the SUT
5. Analyze results

This evaluation section is followed by a guide on using **dtls-fuzzer** which introduces its main use cases.

## Ensuring pre-requisites
**dtls-fuzzer** has been tested on a Ubuntu 18.04 distribution. It should work on any recent Linux distribution. Support for other platforms has not been tested.
It is assumed that a recent (>=8) JDK distribution of Java VM is installed, plus associated utilities (maven).
We recommend using sufficiently strong hardware, otherwise sensitive timing parameters such as response waiting time might become too low, causing learning to fail.
The original experiments were run on a many-core server, however, we expect (though haven't tested thoroughly) that learning should be possible also on a dual-core or quad-core (virtual-)machine with 4 GB of memory or more.
Finally, visualizing models or even exporting them to .pdf requires installing the [graphviz library][graphviz].
It is assumed that the 'dot' utility is located in the system PATH.

## Installing dtls-fuzzer
Run the prepare script which will deploy the local .jars dtls-fuzzer depends to your local maven repository, then install the tool itself.
Thus on a POSIX system would be:

    > bash prepare.sh
    > mvn clean install

Following these steps, a 'target' directory should have been built containing 'dtls-fuzzer.jar'.
From this point onward it is assumed that commands are run from **dtls-fuzzer**'s root directory. 

## Setting up the SUT
We provide a script for setting up the SUT.
This script downloads the source files, installs some dependencies (jvm) and builds the SUT.
To view SUTs for which automatic setup is provided run:

    > bash setup_sut.sh

To set up, for example, contiki-ng's tinydtls implementation run:

    > bash setup_sut.sh ctinydtls

The script will generate two folders in **dtls-fuzzer** root directory.

- 'suts', where the SUT binaries are deployed
- 'modules', where any dependencies are deployed

Unfortunately, automating SUT setup is a complicated process, hence we take the following shortcuts. 
For Java SUTs we don't build the implementations, instead we use the compiled .jars from the 'experiments/suts' directory.
Note that the source code of the SUTs (server applications) is publically available online, see [Scandium][scandium] and [JSSE][jsse].
Also, we don't currently automatically install dependencies.
Unmet dependencies will cause building to fail.
GnuTLS in particular relies on several libraries which will have to be installed manually.
Finally, we do not provide automatic setup for NSS and PionDTLS due to how complicated setup for these systems is.
If things in the setup process stop working, deleting the 'suts' folder (or the 'suts/SUT' folder specific to the SUT) and re-running the setup script may solve the problem.

## Learning a SUT configuration
We are now ready to learn an SUT configuration.
Argument files for various SUT configurations are provided in the 'args' directory located in **dtls-fuzzer**'s home directory.
Each argument filename describes the experiment setup (SUT, alphabet, authentication) as described by the output folder names in 'experiments/results/'.
To start learning for an SUT using a argument file, run:

    > java -jar target/dtls-fuzzer.jar @args/sut_name/arg_file

The output folder will be stored in a generated 'output' directory.

### Concurrent experiments and port collisions
It is possible to learn multiple SUTs at a time assuming that different server listening ports are used. 
However, running more than a few (>2) instances increase the chance of learning failure due to accidental port collision.
In most configurations, servers are configured to listen to some hard-coded port over localhost, the configurations provided use distinct hard-coded ports. 
For JSSE and Scandium configurations, where custom server programs were provided, servers dynamically select the listening port and communicate it over TCP sockets to **dtls-fuzzer**.
This has the advantage of letting **dtls-fuzzer** know for sure when the server is ready to receive packets (rather than having to blindly wait an arbitrary amount of time for the server to start).
The downside is that the allocated port might be the same as some hard-coded port of a different experiment, wherein server thread has recently been stopped and no new thread has been started yet (hence the listening port is free to be allocated).
To avoid this form of collision, we suggest running Scandium and JSSE experiments seperately from all others.


## Suggested configurations
We suggest the following configurations for which automatic building is reliable, learning is faster or interesting bugs have been found.
Make sure you set up the SUT before running the command provided.

### OpenSSL 1.1.1b
Any openssl-1.1.1b configuration (for example 'args/openssl-1.1.1b/learn_openssl-1.1.1b_all_cert_req_rwalk_incl') can be tried out.
The SUT exhibits stable timing making non-determinism unlikely,
Experiments terminate quickly,  exercising all key exchange algorithms. 
Command for client certificate required configuration using all (PSK, RSA, ECDH, DH) key exchange algorithms:

    > java -jar target/dtls-fuzzer.jar @args/openssl-1.1.1b/learn_openssl-1.1.1b_all_cert_req_rwalk_incl 

### MbedTLS 2.16.1
Any mbedtls-2.16.1 configuration can be used for the same reasons as OpenSSL. 
Experiments take more time to complete since the SUT is slower.
Command for client certificate authentication disabled configuration using all key exchange algorithms:

    > java -jar target/dtls-fuzzer.jar @args/mbedtls-2.16.1/learn_mbedtls_all_cert_none_rwalk_incl

### Scandium PSK (before bug fixes)
A redacted version of the model obtained for this configuration appears in the paper.
The model exposes important bugs.
The experiments  should not be run in parallel with experiments not involving Scandium or JSSE.
Command:

    > java -jar target/dtls-fuzzer.jar @args/scandium-2.0.0/learn_scandium-2.0.0_psk_rwalk

### JSSE 12.0.2 with authentication required 
A redacted version of the model obtained for this configuration appears in the paper.
The model exposes important bugs.
The experiments  should not be run in parallel with experiments not involving Scandium or JSSE.
Note that learning for this system does not finish/converge, and will continue on endlessly, building hypotheses with more and more states. 
We hence configured experiments to automatically terminate after one day.
Command for RSA key exchange:

    > java -jar target/dtls-fuzzer.jar @args/jsse-12/learn_jsse-12_rsa_cert_req_rwalk_incl 

## Analyzing results
Once learning is done, things to analyze in the output directory are:
- 'statistics.txt', experiment statistics such as the total number of tests, learning time
- 'nondet.log', logs of encountered non-deterministic behavior, if all went fine this should be empty
- 'learnedModel.dot', the learned model (or final hypothesis) generated on successful termination,
- 'hyp[0-9]+.dot', intermediate hypotheses
- 'error.msg', in case something bad happened causing learning to stop. Also generated if the learning experiment times out.

### Visualizing the model
The .dot learned model can be visualized using the graphviz library, by conversion to .pdf:

    > dot -Tpdf learnedModel.dot  > learnedModel.pdf


# General dtls-fuzzer walkthrough

## Displaying help page
Run:

    > java -jar target/dtls-fuzzer.jar -help

## Learning DTLS implementations

The number of options might be overbearing. 
To learn a DTLS server implementation, one only needs to specify a few options, namely: "-connect ip_address:port" which is the address the running DTLS server is listening to.
All other options are set to default values, including the alphabet.

### Single learning run
To launch a learning run for an existing say local server implementation listening at port 20000, run:

    > java -jar target/dtls-fuzzer.jar -connect localhost:20000

There will likely be issues with this type of learning. 
Learning requires that one is able to reset the server after each test.
Some servers will carry some state from one test to another. 
This may lead to non-determinism during learning, hence a better approach is launching a new server thread on every test using a provided command.
The server thread is killed once the test is run, ensuring proper reset.
Example for OpenSSL:

    > java -jar target/dtls-fuzzer.jar -connect localhost:20000 -cmd "openssl s_server -accept 20000 -dtls1_2"
    
With so many paraments, commands can become very long. 
dtls-fuzzer uses JCommander to parse arguments, which can also read parameters from a file.
Go to 'experiments/args' for examples of arguments.
To supply an argument file to dtls-fuzzer provide it as parameter prepended by "@".
You can also add other explicit arguments to commands (which will overwrite those in the arguments file)

    > java -jar target/dtls-fuzzer.jar @arg_file ...overwriting params...

### Batch learning     
To launch a batch of learning runs, one can use the 'launcher.py' script in 'experiments/scripts'. 
Provided a directory with argument files, the tool will launch a learning process for each argument file.

    > python3 launcher.py -a args_dir
    
## Running a single test
To run a single test on a server using a default alphabet/one provided, you can run:

    > java -jar target/dtls-fuzzer.jar -connect localhost:20000 -test test_file
    
For example of test files, go to 'examples/tests'. 
A test file comprises a newline separated list of inputs. 
The end of the test is either the end of the file, or an empty new line.
"#" is used to comment out a line.

If you have a model/specification, you can also run the test and compare the output with that in a specification.

    > java -jar target/dtls-fuzzer.jar -connect localhost:20000 -test test_file -specification model

Finally, if you have the arguments file for a learning experiment, you can use them to run a test on the SUT involved by just adding the necessary test arguments: 

    > java -jar target/dtls-fuzzer.jar @learning_arg_file -test test_file

This provides a useful means of debugging learning experiments, i.e. finding out what went wrong, or of simply ensuring that parameters in the argument file are correctly configured.


[graphviz]:https://www.graphviz.org
[jsse]:https://github.com/pfg666/jsse-dtls-server
[scandium]:https://github.com/pfg666/scandium-dtls-server