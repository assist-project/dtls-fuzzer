**dtls-fuzzer** is a Java tool which performs protocol state fuzzing of DTLS servers. More concretely, it supports the following functionality:
1. given an alphabet, it can automatically generate a model of a local DTLS server implementation;
2. given a test (sequence of inputs) and an alphabet, it can execute the test on a DTLS server implementation;
3. it can run a batch learning task, involving multiple learning runs.

**dtls-fuzzer** uses [TLS-Attacker][tlsattacker] to generate/parse DTLS messages as well as to maintain state. 
To that end, **TLS-Attacker** has been extended with support for DTLS.
**dtls-fuzzer** relies on version 3.0b of **TLS-Attacker**, a version implementing the DTLS enhancement.

# Artifact contents
The artifact contains:
1. a description of the file structure of **dtls-fuzzer**, including source code and experimental data consistent with that displayed in the paper;
2. a walkthrough for evaluating **dtls-fuzzer** on a chosen SUT (System Under Test)/ DTLS server implementation. 

# dtls-fuzzer file structure
The most important folders in **dtls-fuzzer**'s root directory are:
1. 'src', directory containing the Java source code of **dtls-fuzzer**;
2. 'examples', directory containing examples of alphabets, tests, specifications (i.e. models), and, argument files that can be supplied to **dtls-fuzzer** in order to launch learning experiments. Files in this directory are used as inputs for learning experiments;
3. 'experiments', directory containing data relating to experiments. Some of this data also serves as input for learning experiments. The most notable folders are:
    1. 'suts', with binaries for Java SUTs. These SUTs are custom-made DTLS server programs whose source code is publically available;
    2. 'patches', patches that were applied to some SUTs (particularly to utilities) before the source code was compiled. The primary purpose of these patches was to prevent timing induced-behavior during learning, enable/disable functionality in the SUT, and configure parameters such as the pre-shared key;
    4. 'keystore', key material (e.g. public-private key pairs, Java keystores) used during learning;
    5. 'results', experimental results.

## Experimental results
'experiments/results' contains experimental results, which are the main output of the work. In particular
- 'all_ciphers' contains output folders for all the experiments run;
    - 'mapper' contains experimental results which help justify some of the mapper decisions made (see Section 5.2)
- 'included' contains output folders for converging experiments (converging means that learning successfully generates a model). Note that not all experiments in 'all_ciphers' converged;

### Output folders 
Output folders are named based on the experiment configuration, that is: 
- the SUT/implementation tested;
- the alphabet used;
- where applicable whether client certification was required (req), optional (nreq) or disabled (none);
- the testing algorithm: random walk (rwalk) or an adaptation of it (stests);
    - experiments using the adaptation have not been included in the paper
- optionally, whether retransmissions were included (incl or excl).
    - retransmissions were included by default

As an example, the folder name 'jsse-12_rsa_cert_none_rwalk_incl' indicates an experiment on the JSSE 12 implementation of DTLS, using an alphabet including inputs for performing an RSA handshake, client certificate authentication is disabled, the test algorithm is random walk and retransmissions are included.

An output folder contains:
- 'alphabet.xml', the alphabet;
- 'command.args', the arguments file used;
- 'sul.config', SUT-dependant configuration for **TLS-Attacker**,  the same configuration could be used to execute workflow traces on the SUT using **TLS-Attacker** alone;
- 'hyp[0-9]+.dot', intermediate hypotheses;
- 'statistics.txt', experiment statistics such as the total number of tests, learning time;
    - Table 4 displays this data
- 'nondet.log', logs of encountered non-deterministic behavior;
- 'learnedModel.dot', in case learning converged, the learned model (or final hypothesis);
- 'error.msg', an error message generated in case the experiment failed/learning was stopped and hence, did not converge to a final model. 
    - The main culprit is time-related non-determinism (same inputs lead to different results).

The evaluator can check (for example) that experimental results in 'included' correspond to those displayed in Table 4, or that configurations tested in Table 2 also appear in 'all_ciphers'.
Note that models appearing in the paper were the result of significant pruning. 
Hence the learned models may not be reflective of the models displayed in the paper.

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
It is assumed that a recent 8 JDK distribution of Java VM is installed, plus associated utilities (maven).
We recommend using sufficiently strong hardware, otherwise sensitive timing parameters such as response waiting time might become too low, causing learning to fail.
The original experiments were run on a many-core server, however, we expect (though haven't tested thoroughly) that learning should be possible on a desktop with an i7 processor.
Learning is also possible on weaker systems if timing parameters are tweaked accordingly.
Finally, visualizing models or even exporting them to .pdf requires installing the [graphviz library][graphviz].
It is assumed that the 'dot' utility is located in the system PATH.

In a nutshell, the advised pre-requisites are:

- recent Linux distribution
- desktop CPU or stronger for reliable learning
- (>=) 4 GB RAM
- JVM 8, maven
- graphviz

## Installing dtls-fuzzer
Run the prepare script which will deploy the local .jars **dtls-fuzzer** depends to your local maven repository, then install the tool itself.
Thus on a POSIX system would be:

    > bash prepare.sh
    > mvn clean install

Following these steps, a 'target' directory should have been built containing 'dtls-fuzzer.jar'.
From this point onward it is assumed that commands are run from **dtls-fuzzer**'s root directory. 

## Quickrun
Assume we want to generate a model for Contiki-NG TinyDTLS using PSK.
A quickrun of **dtls-fuzzer** goes as follows.

First we set up the SUT, which is automatically by a 'setup_sut.sh' script.

    > bash setup_sut.sh ctinydtls
    
Then we select an argument file form the 'args/ctinydtls' folder, where ctinydtls is just a shorthand for the implementation.
We notice there are several argument files to chose from, namely:

    learn_ctinydtls_ecdhe_cert_none_rwalk  
    learn_ctinydtls_ecdhe_cert_req_rwalk  
    learn_ctinydtls_psk_rwalk

Only one is of interest, since its filename indicates PSK. 
We thus select it, and run the fuzzer on it.
We additionally cap number of tests to 3000, to shorten learning time.
The command to execute becomes:

    > java -jar target/dtls-fuzzer.jar @args/ctinydtls/learn_ctinydtls_psk_rwalk -queries 3000

We notice that an output directory, 'output/ctinydtls_psk_rwalk' for the experiment has been created.
We can 'ls' this directory to check on the status of the experiment (the number of hypotheses generated...).

    > ls output/ctinydtls_psk_rwalk


### When things go right
If all goes well, after many hours, the output directory should contain a 'learnedModel.dot' file.
We can visualize the file using the graphviz 'dot' utility, by exporting to .pdf and opening the .pdf with our favorite .pdf viewer.

    > dot -Tpdf output/learnedModel.dot > output/learnedModel.pdf
    > evince output/learnedModel.pdf

Finally, we can use 'trim_model.sh' to generater a better/trimmer version of the model.
The commands for are:

    > bash trim_model.sh output/learnedModel.dot output/nicerLearnedModel.dot
    > dot -Tpdf output/nicerLearnedModel.dot > output/nicerLearnedModel.pdf
    > evince output/nicerLearnedModel.pdf

We can now determine conformance of the system by checking the model against the specification...

### When things go wrong
While 'ls'-ing the output directory we might find 'error.msg'. 
That's a sign that the experiment failed and learning terminated abruptly
In such cases displaying the contents reveals the reason behind the failure

    > cat output/error.msg
    
Note that checking conformance can still be performed on the last generated hypothesis, as long as potential findings are validated against the system (as they should be anyway).


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
For Java SUTs (JSSE, Scandium) we don't build the implementations, instead we use the compiled .jars from the 'experiments/suts' directory.
Note that the source code of the SUTs (server applications) is publically available online, see [Scandium][scandium] and [JSSE][jsse].
Also, dependencies are automatically installed using may prompt 'sudo' access.
This is the case for GnuTLS which relies on the external libnettle.
Finally, we do not provide automatic setup for NSS and PionDTLS due to how complicated setup for these systems is.

### Troubleshooting
If things in the setup process stop working, deleting the 'suts' folder (or the 'suts/SUT' folder specific to the SUT) and re-running the setup script may solve the problem.

## Learning an SUT configuration
We are now ready to learn an SUT configuration.
Argument files for various SUT configurations are provided in the 'args' directory located in **dtls-fuzzer**'s home directory.
Each argument filename describes the experiment setup (SUT, alphabet, authentication) as described by the output folder names in 'experiments/results/'.
To start learning for an SUT using a argument file, run:

    > java -jar target/dtls-fuzzer.jar @args/sut_name/arg_file

The output folder will be stored in a generated 'output' directory.

### Parameter adaptations
Compared to experiments in the paper, we increased the response timeout for several SUTs as an adaptation to less powerful hardware.
To shorten learning time, we suggest decreasing the test bound of the random walk algorithm from 20000 to 5000.
This can be done by:

    > java -jar target/dtls-fuzzer.jar @args/sut_name/arg_file -queries 5000

This will overwrite the bound setting in the argument file. 
Aside from GnuTLS, PionDTLS and JSSE, we expect learning to produce the same models for this lower bound.

Timing can become an issue, causing non-determinism, followed by abrupt termination with an informative 'error.msg' file.
In such cases, there are two knobs which can be tweaked: 

1. the *response timeout* (time waited for each response before concluding that server is silent);
2. the *start timeout* (time waited for the server to start).

These parameters can be adjusted by overwriting (likely with a higher value) the corresponding settings in the argument file:

    > java -jar target/dtls-fuzzer.jar @args/sut_name/arg_file -timeout new_response_timeout -runWait new_start_timeout

### Concurrent experiments and port collisions
It is possible to run multiple experiments at a time provided that servers are configured to listen to different ports. 
A simple way is via the 'disown' utility, for example:

    > java -jar target/dtls-fuzzer.jar @args/ctinydtls/learn_ctinydtls_psk_rwalk > /dev/null 2>&1 & disown

However, running more than a few (>2) instances increases the chance of learning failure due to accidental port collision.
In most configurations, servers are configured to listen to some hard-coded port over localhost, the configurations provided using distinct hard-coded ports. 
For JSSE and Scandium configurations, the setup is different. 
On every test, the SUT launches a server listening to a dynamically chosen port and communicates the port over TCP sockets to **dtls-fuzzer**.
This has the advantage of notifying **dtls-fuzzer** when the server is ready to receive packets (lacking this, **dtls-fuzzer** would have to blindly wait an arbitrary amount of time for the server to start).
The downside is that the allocated port might be the same as some hard-coded port of a different experiment, wherein a server thread has recently been stopped and a new thread has not been started yet (mearning the hard-coded port could be used in dynamic allocation).
To avoid this form of collision, we advise running Scandium and JSSE experiments seperately from all others.


## Suggested configurations
We suggest the following configurations for which automatic building is reliable, learning is faster or interesting bugs have been found.
Make sure you set up the SUT before running the command provided.
You will note we focus on PSK configurations where possible.
That is because PSK with small passwords requires significantly less processing time than any other encryption mechanism.


### OpenSSL 1.1.1b
Any openssl-1.1.1b configuration (for example 'args/openssl-1.1.1b/learn_openssl-1.1.1b_all_cert_req_rwalk_incl') can be tried out.
The SUT exhibits stable timing making non-determinism unlikely,
Experiments terminate quickly (~half a day),  exercising all key exchange algorithms. 
Command for client certificate required configuration using all (PSK, RSA, ECDH, DH) key exchange algorithms:

    > LD_LIBRARY_PATH=suts/openssl-1.1.1b/ java -jar target/dtls-fuzzer.jar @args/openssl-1.1.1b/learn_openssl-1.1.1b_all_cert_req_rwalk_incl 

Note, when learning OpenSSL it is necessary to point the LD_LIBRARY_PATH variable to the installation directory.

### MbedTLS 2.16.1
Any mbedtls-2.16.1 configuration can be used for the same reasons as OpenSSL. 
Experiments take more time to complete since the SUT is slower.
Command for client certificate authentication disabled configuration using all key exchange algorithms:

    > java -jar target/dtls-fuzzer.jar @args/mbedtls-2.16.1/learn_mbedtls_all_cert_none_rwalk_incl


### Contiki-NG TinyDTLS using PSK
A redacted version of the model obtained for this configuration appears in the appendix.

    > java -jar target/dtls-fuzzer.jar @args/ctinydtls/learn_ctinydtls_psk_rwalk

### Scandium PSK (before bug fixes)
A redacted version of the model obtained for this configuration appears in the paper.
The model exposes important bugs.
The experiment should not be run in parallel with experiments not involving Scandium or JSSE.
Command:

    > java -jar target/dtls-fuzzer.jar @args/scandium-2.0.0/learn_scandium-2.0.0_psk_rwalk

### JSSE 12.0.2 with authentication required 
A redacted version of the model obtained for this configuration appears in the paper.
The model exposes important bugs.
The experiment should not be run in parallel with experiments not involving Scandium or JSSE.
Note that learning for JSSE does not finish/converge, building hypotheses with more and more states. 
We hence configured JSSE experiments to automatically terminate after one day (two days in the paper).
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

Unfortunately, as models grow is size, the .pdfs generated using this method become increasingly difficult to read. 

Hence we developed/used/imported prunning scripts which are accessed by 'trim_model.sh'. 
We advise using it in its most basic form, whic is:

    > bash trim_model.sh learnedModel.dot trimmedLearnedModel.dot 

The script:

1. compactifies states and input/output labels
2. colors paths leading to handshake completion
    - the user should then determine whether the handshakes were legal given the configuration
3. merges 3 or more transitions connecting the same state states with same outputs but different inputs, under the 'Other' input
4. (optionally) prunes states from which a handshake can no longer be completed (particularly useful for JSSE)
5. (optionally) positions transitions connecting the same states on a single edge

(5) requires installing the custom mypydot library found in 'experiments\scripts' which requires Python 3. 
All other steps use plain 'sed' plus the [dot-trimmer][dottrimmer] Java library. 
A .jar for this library is included in 'experiments\scripts'.


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

    > python3 experiments/scripts/launcher.py --jar target/dtls-fuzzer.jar --args args_folder

## Running a test suite
Before running learning experiments, it helps to check that arguments are correctly set, particularly timing parameters.
To that end, **dtls-attacker** can execute a custom test suite (collection of tests) on the SUT, and provide a summary of the outputs.
This functionality can also be used when diagnozing failed learning experiments, i.e. finding out what went wrong.

To run the test suite on a server using a default alphabet, you can run:

    > java -jar target/dtls-fuzzer.jar -connect localhost:20000 -test test_file
    
For example of test files, go to 'examples/tests'. 
A test file comprises a newline-separated list of inputs. 
Tests are seperated by empty new lines.
The end of each test is either the end of the file, or an empty new line.
"#" is used to comment out a line.

If you have a model/specification, you can also run the test suite and compare the output with that in a specification.

    > java -jar target/dtls-fuzzer.jar -connect localhost:20000 -test test_file -specification model
    
The number of times tests are run is configurable by the '-times' parameter, which defaults to 1.
Setting it to a high number helps detect non-determinism in learning configurations, by comparing the output of each test.

    > java -jar target/dtls-fuzzer.jar -connect localhost:20000 -test test_file -times 10

Finally, if you have the arguments file for a learning experiment, you can use them to run tests on the SUT involved by just adding the necessary test arguments: 

    > java -jar target/dtls-fuzzer.jar @learning_arg_file -test test_file



[tlsattacker]:https://github.com/RUB-NDS/TLS-Attacker
[graphviz]:https://www.graphviz.org
[jsse]:https://github.com/pfg666/jsse-dtls-server
[scandium]:https://github.com/pfg666/scandium-dtls-server
[dottrimmer]:https://github.com/pfg666/dot-trimmer
