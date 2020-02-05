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
2. a **dtls-fuzzer** walkthrough, consisting of a step-by-step guideto generating a model for a given SUT (System Under Test)/ DTLS server implementation. 

# dtls-fuzzer file structure
There are many files/folders, the most relevant are:
1. 'src', directory containing the Java source code of **dtls-fuzzer**. 
2. 'examples', directory containing examples of alphabets, tests, specifications (i.e. models) and example of arguments that can be supplied to **dtls-fuzzer**, to launch learning experiments. Files here are used as inputs for learning experiments.
3. 'experiments', directory containing data pertaining to experiments. Some of this data also serves as input during learning experiments. The most notable folders are:
    1. 'suts', with binaries for Java SUTs. These SUTs are custom-made DTLS server programs whose source code is publically available. 
    2. 'patches', patches that were applied to some SUTs (particularly to utilities) before the source code was compiled. The primary purpose of these patches was to prevent timing induced-behavior during learning. They also unlocked functionality in the SUT.
    3. 'results', the experimental results
    4. 'keystore', key material (e.g. public-private key pairs, Java keystores) used during learning
    5. 'results', experimental results following the learning

## Experimental results
'experiments/results' contains experimental results, which are the main output of the work. In particular
- 'all_ciphers' contains output folders for all the experiments run
    - 'mapper' contains output folders for experiments which help justify some of the mapper decisions made ()
- 'included' contains output folders for converging experiments (converging means that learning successfully generates a model). Note that not all experiments in 'all_ciphers' converged
- 'archive' contains previous experiments not considered in the work

Output folders are named based on the experiment configuration, that is: 
- the SUT/implementation tested, 
- the alphabet used, 
- where applicable whether client certification was required (req), optional (nreq) or disabled (none), 
- the testing algorithm: random walk (rwalk) or and adaptation of it (stests). Experiments using the adaptation have not been considered in the paper.
- optionally, whether retransmissions were included (incl or excl). Retransmissions were included by default. 

An output folder contains of:
- 'alphabet.xml', the alphabet 
- 'command.args', the arguments used
- 'sul.config', SUT-dependant configuration for **TLS-Attacker**,  the same configuration could be used to execute workflow traces on the SUT using **TLS-Attacker** alone
- 'hyp[0-9]*.dot', intermediate hypotheses 
- 'statistics.txt', experiment statistics such as the total number of tests, learning time. Table 4 displays this data
- 'nondet.log', logs of encountered non-deterministic behavior
- 'learnedModel.dot', in case learning converged, the learned model (or final hypothesis)
- 'error.msg', an error message in case learning did not converge, either because it was stopped or because an exception happened. 

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

## Ensuring pre-requisites
**dtls-fuzzer** has been tested on a Ubuntu 18.04 distribution. It should work on any recent Linux distribution. Support for other platforms has not been tested.
It is assumed that a recent (>=8) JDK distribution of Java VM is installed, plus associated utilities (maven).
We recommend using sufficiently strong hardware, otherwise timing parameters might become an issue.
The original experiments were run on a many-core server, however, we expect (though haven't tested) that learning should be possible also on a dual-core or quad-core (virtual-)machine with 4 GB of memory or more.

## Installing dtls-fuzzer
Run the prepare script which will deploy the local .jars dtls-fuzzer depends to your local maven repository, then install the tool itself.
Thus on a POSIX system would be:

    > bash prepare.sh
    > mvn clean install

Following these steps, a 'target' directory should have been built containing dtls-fuzzer.jar . 

## Setting up the SUT
We provide a script for setting up the SUT, which encompasses downloading the source files, installing any dependencies and building the SUT.
To view SUTs for which automatic setup is provided run:

    > bash setup_sut.sh

To setup, for example, contiki-ng's tinydtls implementation run:

    > bash setup_sut.sh ctinydtls

The script will generate two folders in **dtls-fuzzer** root directory.

- 'suts', where the SUT binaries are deployed
- 'modules', where any dependencies are deployed

Unfortunately, automating SUT setup can be a complicated process, hence we take the following shortcuts. 
For the Java libraries we don't build the implementations, instead we use the compiled .jars from the 'experiments/suts' directory.
Right now we don't automatically install dependencies.
Unmet dependencies will cause building to fail.
GnuTLS in particular relies on several libraries which will have to be installed manually.
Finally, we do not provide automatic setup for NSS and PionDTLS due to how complicated setup for these systems is.
If things stop working, deleting the 'suts' folder (or the 'suts/SUT_SPEC' folder) might so

## Learning a SUT configuration
We are now ready to learn a SUT configuration.
Configuration files are provided in the 'args' directory located in **dtls-fuzzer**'s home directory.
Each configuration filename describes the experiment setup (SUT, alphabet, authentication) in the same way it is described in the experimental results output folder name.
To start learning for a SUT using a configuration file, run:

    > java -jar target/dtls-fuzzer.jar @config_file

It is possible to learn multiple SUTs at a time assuming that different server listening ports are used. 
However, running more than a few (>3) instances may lead to learning failure due to port collision.

## Suggested configurations
With learning taking more than a day and with SUT-setup challenges, we suggest the running following configurations. 




# Displaying help page
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
This may lead to non-determinism during learning, hence it is suggested that a command is provided by which the server can be launched.
A new server thread will be then launched on every test and killed once the test is run, ensuring proper reset.
Example for OpenSSL:

    > java -jar dtls-fuzzer.jar -connect localhost:20000 -cmd "openssl s_server -accept 20000 -dtls1_2"
    
With so many paraments, commands can get very long. 
dtls-fuzzer uses JCommander to parse arguments, which can also read parameters from a file.
Go to 'experiments/args' for examples of arguments.
To supply an argument file to dtls-fuzzer provide it as parameter prepended by "@".
You can also add other explicit arguments to the commands (which will overwrite those in the arguments file)

    > java -jar dtls-fuzzer.jar @arg_file ...overwriting params...

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






