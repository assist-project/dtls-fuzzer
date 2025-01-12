**DTLS-Fuzzer** is a tool for protocol state fuzzing of DTLS servers and clients.
More concretely, it supports the following functionality:
1. given an alphabet, it can automatically generate a model of a local DTLS server/clients implementation;
2. given a test (sequence of inputs) and an alphabet, it can execute the test on a DTLS server implementation;
3. it can run a batch learning task, involving multiple learning runs.

DTLS-Fuzzer uses [TLS-Attacker][tlsattacker] to generate and parse DTLS messages as well as to maintain state.
To that end, TLS-Attacker has been extended with support for DTLS, starting from [TLS-Attacker version 3.0b][tlsattackerver].

# Artifact contents
The artifact contains:
1. a description of the file structure of DTLS-Fuzzer, including source code and experimental data consistent with that displayed in the paper;
2. a walkthrough for evaluating DTLS-Fuzzer on a chosen SUT (System Under Test) DTLS server implementation.

## File structure
The most important folders in DTLS-Fuzzer's root directory are:
1. `src`, containing the Java source code;
2. `examples`, containing examples of alphabets, tests, specifications (i.e. models), and, argument files that can be supplied to DTLS-Fuzzer in order to launch learning experiments. Files in this directory are used as inputs for learning experiments;
3. `experiments`, containing data relating to experiments. Some of this data also serve as input for learning experiments. The most notable folders are:
    1. `suts`, with binaries for Java SUTs. These SUTs are custom-made DTLS server programs whose source code is publicly available;
    2. `patches`, patches that were applied to some SUTs (particularly to utilities) before the source code was compiled. The primary purpose of these patches was to prevent timing induced-behavior during learning, enable/disable functionality in the SUT, and configure parameters such as the pre-shared key;
    4. `keystore`, key material (e.g., public-private key pairs, Java keystores) used during learning;
    5. `results`, experimental results.

## Experimental results
`experiments/results` contains experimental results, which are the main output of using the tool. In particular
- `all_ciphers` contains output folders for all the experiments run;
    - `mapper` contains experimental results which help justify some of the mapper decisions made (see Section 5.2);
- `included` contains output folders for converging experiments (converging means that learning successfully generates a model).
Note that not all experiments in `all_ciphers` were successful/terminated with a final model (we say in such cases that model learning did not converge).

### Output folders
Output folders are named based on the experiment configuration, that is:
- the SUT/implementation tested;
- whether the SUT is a server or a client;
- the alphabet used, in terms of key exchange algorithms covered, where `all` indicates all four key exchange algorithms were used;
- where applicable whether client certification was required (`req`), optional (`nreq`) or disabled (`none`);
- optionally, whether retransmissions were included in/excluded from outputs (`incl` or `excl`).
    - retransmissions were included by default

As an example, the folder name `jsse-12_server_rsa_cert_none_incl` indicates an experiment on the JSSE 12 server implementation of DTLS, using an alphabet including inputs for performing RSA handshakes, client certificate authentication is disabled, and that retransmissions are included.

An output folder contains:
- `alphabet.xml`, the input alphabet;
- `command.args`, the arguments file used containing various experiment parameters, most notably:
    - *eqvQueries*, the bound on the number of equivalence queries (random walk tests) which need to pass for a hypothesis to be deemed final
    - *equivalenceAlgorithms*, model-based test algorithms employed
    - *startWait* and *responseWait*, the start and response timeout respectively (we touch on them later)
- `sul.config`, SUT-dependent configuration for TLS-Attacker, the same configuration can be used to execute workflow traces on the SUT using TLS-Attacker alone;
- `hyp[0-9]+.dot`, intermediate hypotheses;
- `statistics.txt`, experiment statistics such as the total number of tests, learning time;
    - Table 4 displays this data
- `nondet.log`, logs of encountered non-deterministic behavior;
- `learnedModel.dot`, in case learning converged, the learned model (i.e., the final hypothesis);
- `error.msg`, an error message generated in case the experiment failed/learning was stopped and hence, did not converge to a final model.
    - the main culprit is time-related non-determinism (same inputs lead to different results).

The evaluator can check (for example) that experimental results in `included` correspond to those displayed in Table 4, or that configurations tested in Table 2 also appear in `all_ciphers`.
Note that models appearing in the paper were the result of significant pruning/trimming, whereas the models appearing in output folders are unaltered.

# DTLS-Fuzzer evaluation steps
For the purpose of evaluating DTLS-Fuzzer it is necessary to perform the following steps:
1. Ensure pre-requisites are met
2. Install DTLS-Fuzzer
3. Setup SUT
4. Use DTLS-Fuzzer to generate models for the SUT
5. Analyze results

This evaluation section is followed by a guide on using DTLS-Fuzzer which introduces its main use cases.

## Ensuring pre-requisites
DTLS-Fuzzer has been tested on Ubuntu 18.04 and Debian 9-12 distributions of Linux.
It should work on any recent Linux distribution.
Support for other platforms has not been tested.
This guide assumes a Debian-based distribution is used (which has `apt-get`).

Java 17 JDK (Java Development Kit) Virtual Machine (VM) or later is required.
We also rely on maven (the `mvn` utility) for dependency management and deployment.

We recommend using a sufficiently powerful machine, otherwise sensitive timing parameters such as response waiting time, might become too low, causing different outputs to the ones obtained in the paper.
Worse yet, they can cause learning  experiments to fail.
The original experiments were run on a many-core server, however, we expect (though have not tested thoroughly) that learning should be possible on a desktop with an i7 processor.
Learning is also possible on weaker systems if timing parameters are tuned accordingly.
Finally, visualizing .dot models by exporting them to .pdf requires installing the [graphviz library][graphviz].
It is assumed that the `dot` utility provided by graphviz is located in the system PATH.

In a nutshell, the advised pre-requisites are:

- recent Linux distribution, preferably Debian-based
- desktop/server machine for experiment reproduction/reliable learning
- (>=) 4 GB RAM
- Java 17 JDK
- maven
- graphviz

## Setting up the environment
### Java 17 JDK
DTLS-Fuzzer requires JDK (Java Development Kit) version 17 or later.
If Java is not installed, we install the OpenJDK implementation, and can skip the rest of this subsection.

    sudo apt-get install openjdk-17-jdk

If a version of java is installed, we can check which version it is by running:

    java -version

### Others
With JDK set, we proceed to install the other dependencies, maven, graphviz plus some common SUT dependencies.
We then clone DTLS-Fuzzer's repository to a folder of choice, checking out the artifact branch.
To finish, we make that folder our current directory.

    sudo apt-get install maven graphviz autotools-dev automake libtool
    git clone -b usenix20-artifact https://github.com/assist-project/dtls-fuzzer.git
    cd dtls-fuzzer

## Installation
Run the `install.sh` script which installs libraries DTLS-Fuzzer depends on, and the tool itself.

    ./install.sh

Following these steps, a directory named `target` should have been built containing `dtls-fuzzer.jar`.
This is our executable library.
From this point onward it is assumed that commands are run from DTLS-Fuzzer's root directory.

## Quickrun
Suppose we want to generate a model for OpenSSL 1.1.1b using only PSK (Pre-Shared Keys).
A quickrun of DTLS-Fuzzer goes as follows.

First we set up the SUT, which is automatically by a `setup_sut.sh` script.

    ./setup_sut.sh openssl-1.1.1b

This will create an empty `modules` directory, and a `suts/openssl-1.1.1b` directory.

Then we select an argument file from the `args/openssl` folder.

We notice there are several argument files to choose from, namely:

    learn_openssl_client_dhe_ecdhe_rsa_cert
    learn_openssl_client_dhe_ecdhe_rsa_cert_reneg
    learn_openssl_client_ecdhe_cert_reneg
    learn_openssl_client_psk
    learn_openssl_client_psk_reneg
    learn_openssl_server_all_cert_none
    learn_openssl_server_all_cert_nreq
    learn_openssl_server_all_cert_req
    learn_openssl_server_psk

The argument file of interest is `learn_openssl_server_psk`, since its filename indicates PSK.
We thus select it, and run the fuzzer on it.
We additionally cap the number of tests to 200, to shorten learning time.
Finally, for OpenSSL, LD_LIBRARY_PATH has to be set to the implementation's directory (`suts/openssl-1.1.1b/`).
Before we run learning, we might want to execute a simple test to check that our setup is functioning.
A good test is just completing a handshake.
We supply the argument file, along with a corresponding test from `examples/tests/servers` as parameter.
We get:

    LD_LIBRARY_PATH=suts/openssl-1.1.1b/ java -Dopenssl.version=1.1.1b -jar target/dtls-fuzzer.jar @args/openssl/learn_openssl_server_psk -test examples/tests/servers/psk

If all goes well, the server should have printed out "This is a hello message", a message we send after completing the handshake.
Knowing our setup functions, we can now start learning by running:

    LD_LIBRARY_PATH=suts/openssl-1.1.1b/ java -Dopenssl.version=1.1.1b -jar target/dtls-fuzzer.jar @args/openssl/learn_openssl_server_psk -eqvQueries 200

We notice that an output directory, `output/openssl-1.1.1b_server_psk/` for the experiment has been created.
We can `ls` this directory to check the current status of the experiment (the number of hypotheses generated...).

    ls output/openssl-1.1.1b_server_psk/


### When things go right
If all goes well, after 20-30 minutes, the output directory should contain a `learnedModel.dot` file.
We can visualize the file using the graphviz `dot` utility, by exporting to .pdf and opening the .pdf with our favorite .pdf viewer.

    dot -Tpdf output/openssl-1.1.1b_server_psk/learnedModel.dot > output/openssl-1.1.1b_server_psk/learnedModel.pdf
    evince output/openssl-1.1.1b_server_psk/learnedModel.pdf

Finally, we can use `trim_model.sh` to generated a better/trimmer version of the model.
This can be done as follows:

    ./trim_model.sh --output output/openssl-1.1.1b_server_psk/nicerLearnedModel.dot output/openssl-1.1.1b_server_psk/learnedModel.dot
    dot -Tpdf output/openssl-1.1.1b_server_psk/nicerLearnedModel.dot > output/openssl-1.1.1b_server_psk/nicerLearnedModel.pdf
    evince output/openssl-1.1.1b_server_psk/nicerLearnedModel.pdf

We can now determine conformance of the system by checking the model against the specification...

### When things go wrong
When `ls`-ing the output directory we might find `error.msg`.
That's a sign that the experiment failed and learning terminated abruptly
In such cases, displaying the contents reveals the reason behind the failure

    cat output/openssl-1.1.1b_server_psk/error.msg

Note that checking conformance can still be performed on the last generated hypothesis, as long as potential findings are validated against the system (as they should be anyway).

## Setting up the SUT
We provide a script for setting up the SUT.
This script downloads the source files, installs some dependencies (JVM) and builds the SUT.
To view SUTs for which automatic setup is provided run:

    ./setup_sut.sh

To set up, for example, Contiki-NG's tinydtls implementation run:

    ./setup_sut.sh ctinydtls

The script will generate two folders in DTLS-Fuzzer's root directory.

- `suts`, where the SUT binaries are deployed
- `modules`, where any dependencies are deployed

Unfortunately, automating SUT setup is a complicated process, hence we take the following shortcuts.
For Java SUTs (JSSE, Scandium) we don't build the implementations, instead we use the compiled .jars from the `experiments/suts` directory.
Note that the source code of these Java SUTs (server applications) is publicly available online, see [Scandium][scandium] and [JSSE][jsse], which is also the case for [PionDTLS][piondtls].
Automatically installing dependencies may prompt `sudo` access.
This happens for GnuTLS, which relies on external libraries such as nettle, and for Eclipse's TinyDTLS, which relies on autoconf.
Finally, we do not provide automatic setup/argument files for NSS and PionDTLS due to how complicated setup for these systems is.

### Troubleshooting
If things in the setup process stop working, deleting the `suts` folder (or the `suts/SUT` folder specific to the SUT) and re-running the setup script may solve the problem.
Also, in case of building failure, the source code of the implementation should still be downloaded to the `suts` directory.
A workaround is to build the implementation manually.
As long as the implementation is built, our setup should work.

We hereby give an incomplete tree of dependencies the various SUTs have.
Those in italics are dependencies which `setup_sut.sh` tries to install using `sudo` access.

- GnuTLS:
    - *m4*
    - *pkg-config*
    - *nettle*
- Eclipse's TinyDTLS
    - *m4*
    - *autoconf*
- WolfSSL
    - *m4*
    - *autoconf*
    - libtool
 - nettle
    - *m4*
    - *pkg-config*
 - autoconf
    - aclocal
        - automake
        - autotools-dev

## Learning an SUT configuration
We are now ready to learn an SUT configuration.
Argument files for various SUT configurations are provided in the `args` directory located in DTLS-Fuzzer's home directory.
Each argument filename describes the experiment setup (SUT, alphabet, authentication) as described by the output folder names in `experiments/results/`.
To start learning for an SUT using a argument file, run:

    java -jar target/dtls-fuzzer.jar @args/sut_name/arg_file

The output folder will be stored in a generated `output` directory.

### Parameter adaptations
#### Test bound
Compared to experiments in the paper, we increased the response timeout for several SUTs as an adaptation to less powerful hardware.
To shorten learning time, we suggest decreasing the test bound of the random walk algorithm from 20000 to 5000.
This can be done by:

    java -jar target/dtls-fuzzer.jar @args/sut_name/arg_file -eqvQueries 5000

This will overwrite the bound setting in the argument file.
Aside from GnuTLS, PionDTLS and JSSE, we expect learning to produce the same models for this lower bound.

#### Timing parameters
Timing can become an issue, causing non-determinism, followed by abrupt termination with an informative `error.msg` file.
In such cases, there are two knobs which can be tweaked:

1. the *response timeout* (time waited for each response before concluding that the server is silent);
2. the *start timeout* (time waited for the server to start).

These parameters can be adjusted by overwriting (likely with a higher value) the corresponding settings in the argument file:

   java -jar target/dtls-fuzzer.jar @args/sut_name/arg_file -responseWait new_response_timeout -startWait new_start_timeout

To avoid issues to do with timing, we suggest running experiments on a sufficiently powerful machine.
The main cause of non-determinism is the SUT taking too long to start or to generate a response.
This likelihood decreases as more computing power is provided.

#### Learning time
We may wish to automatically terminate experiments after a certain period, particularly experiments that are not expected to ever terminate.
Setting this period is possible via the **time limit** parameter which is assigned the maximum duration the experiment is allowed to run for.
This duration is provided in [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) format.
To cap execution time of an experiment to 60 minutes, we would run:

    java -jar target/dtls-fuzzer.jar @args/sut_name/arg_file -timeLimit "PT60M"

### Concurrent experiments and port collisions
It is possible to run multiple experiments at a time provided that servers are configured to listen to different ports.
We can choose to launch each experiment in a separate terminal.
Alternatively, we can launch experiments in a single terminal using the `disown` utility:

    java -jar target/dtls-fuzzer.jar @args/ctinydtls/learn_ctinydtls_server_psk 1>/dev/null 2>&1 & disown

However, running more than a few (>2) places additional burden on the machine.
It also increases the chance of learning failure due to accidental port collision.
In most configurations, servers are configured to listen to some hard-coded port over localhost, the configurations provided in `args` using distinct hard-coded ports.
For JSSE and Scandium configurations, the setup is different.
On every test, the SUT launches a server listening to a dynamically chosen port and communicates the port over TCP sockets to DTLS-Fuzzer.
This has the advantage of notifying DTLS-Fuzzer when the server is ready to receive packets (lacking this, DTLS-Fuzzer would have to blindly wait an arbitrary amount of time for the server to start).
The downside is that the allocated port might be the same as some hard-coded port of a different experiment, wherein a server thread has recently been stopped and a new thread has not been started yet (meaning the hard-coded port could be used in dynamic allocation).
To avoid this form of collision, we advise running Scandium and JSSE experiments separately from all others.


## Suggested configurations
We suggest the following configurations for which automatic building is reliable, learning is faster or interesting bugs have been found.
Make sure you set up the SUT before running the command provided.
You will note we focus on PSK configurations where possible.
That is because PSK with small passwords requires significantly less processing time than any other encryption mechanism.


### OpenSSL 1.1.1b
Any openssl-1.1.1b configuration (for example `args/openssl/learn_openssl_server_all_cert_req`) can be tried out.
Experiments terminate quickly (less than a day), exercising all key exchange algorithms.
Command for client certificate required configuration using all (PSK, RSA, ECDH, DH) key exchange algorithms:

    LD_LIBRARY_PATH=suts/openssl-1.1.1b/ java -jar target/dtls-fuzzer.jar @args/openssl/learn_openssl_server_all_cert_req -eqvQueries 5000

Note, when learning OpenSSL it is necessary to point the LD_LIBRARY_PATH variable to the installation directory.

### MbedTLS 2.16.1
Any mbedtls-2.16.1 configuration can be used for the same reasons as OpenSSL.
Experiments take more time to complete since the SUT is slower.
Command for client certificate authentication disabled configuration using all key exchange algorithms:

    java -jar target/dtls-fuzzer.jar @args/mbedtls-2.16.1/learn_mbedtls_server_all_cert_none -eqvQueries 5000


### Contiki-NG TinyDTLS using PSK
A redacted version of the model obtained for this configuration appears in the appendix.
We can use a low test bound of 2000 since the input alphabet is small, making testing easier.

    java -jar target/dtls-fuzzer.jar @args/ctinydtls/learn_server_ctinydtls_psk -eqvQueries 2000

### WolfSSL 4.0.0 using PSK
For WolfSSL we provide a PSK configuration for which learning should terminate relatively quickly.

    java -jar target/dtls-fuzzer.jar @args/wolfssl-4.0.0/learn_wolfssl-4.0.0_server_psk -eqvQueries 2000

### GnuTLS 3.6.7 with client authentication disabled
The more recent GnuTLS version we analyzed produced nice, compact models.
Unfortunately, enabling client authentication lead to a sharp increase in the number of required tests.
We suggest a configuration which disables it to shorten learning time:

    java -jar target/dtls-fuzzer.jar @args/gnutls-3.6.7/learn_gnutls-3.6.7_server_all_cert_none -eqvQueries 2000

### Scandium PSK (before bug fixes)
A redacted version of the model obtained for this configuration appears in the paper.
The model exposes important bugs, unfortunately, the experiment is lengthy.
The experiment should not be run in parallel with experiments not involving Scandium or JSSE.
Command:

    java -jar target/dtls-fuzzer.jar @args/scandium-2.0.0/learn_scandium-2.0.0_server_psk -eqvQueries 2000

### JSSE 12.0.2 with authentication required
A redacted version of the model obtained for this configuration appears in the paper.
The model exposes important bugs.
The experiment should not be run in parallel with experiments not involving Scandium or JSSE.
Note that learning for JSSE does not finish/converge, building hypotheses with more and more states.
We hence configured JSSE experiments to automatically terminate after one day (two days in the paper).
Command for RSA key exchange:

    java -jar target/dtls-fuzzer.jar @args/jsse-12/learn_jsse-12_server_rsa_cert_req

Instead of arduous learning, we may want to simply test if a handshake can be completed in this setting without sending any certificate messages.
This can be done by running:

    java -jar target/dtls-fuzzer.jar @args/jsse-12/learn_jsse-12_server_rsa_cert_req -test examples/tests/servers/rsa


## Analyzing results
Once learning is done, things to analyze in the output directory are:
- `statistics.txt`, experiment statistics such as the total number of tests, learning time;
- `nondet.log`, logs of encountered non-deterministic behavior, if all went fine this should be empty;
- `learnedModel.dot`, the learned model (or final hypothesis) generated on successful termination;
- `hyp[0-9]+.dot`, intermediate hypotheses;
- `error.msg`, in case something bad happened causing learning to stop. Also generated if the learning experiment times out.

### Visualizing the model
The .dot learned model can be visualized using the graphviz library, by conversion to .pdf:

    dot -Tpdf learnedModel.dot > learnedModel.pdf

Unfortunately, as models grow is size, the .pdfs generated using this method become increasingly difficult to read.
Hence we developed/used/imported pruning scripts which are accessed by `trim_model.sh`.
The scripts provides usage information by running it without any argument:

    ./trim_model.sh

We advise using the script in its most basic form, which is:

    ./trim_model.sh learnedModel.dot

The script:

1. compactifies states and input/output labels
2. colors paths leading to handshake completion
    - the user should then determine whether the handshakes are legal given the configuration
3. merges groups of 3 or more transitions connecting the same state states, and having the same outputs but different inputs, under the `Other` input
4. (optionally) prunes states from which a handshake can no longer be completed (particularly useful for JSSE)
5. (optionally) positions transitions connecting the same states on a single edge

(5) requires installing the custom mypydot Python 3 library found in `experiments/scripts`.
All other steps use plain `sed` plus the [dot-trimmer][dottrimmer] Java library.
A .jar for this library is included in `experiments/scripts`.

# General walkthrough

## Displaying help page
Run:

    java -jar target/dtls-fuzzer.jar

## Learning DTLS implementations

The number of options might be overbearing.
To learn a DTLS server implementation, one only needs to specify a few options, namely: "-connect ip_address:port" which is the address the running DTLS server is listening to.
All other options are set to default values, including the alphabet.

### Single learning run
To launch a learning run for an existing say local server implementation listening at port 20000, run:

    java -jar target/dtls-fuzzer.jar -connect localhost:20000

There will likely be issues with this type of learning.
Learning requires that one is able to reset the server after each test.
Some servers will carry some state from one test to another.
This may lead to non-determinism during learning, hence a better approach is launching a new server thread on every test using a provided command.
The server thread is killed once the test is run, ensuring proper reset.
Example for OpenSSL:

    java -jar target/dtls-fuzzer.jar -connect localhost:20000 -cmd "openssl s_server -accept 20000 -dtls1_2"

With so many paraments, commands can become very long.
DTLS-Fuzzer uses JCommander to parse arguments, which can also read parameters from a file.
Go to `args` for examples of arguments.
To supply an argument file to DTLS-Fuzzer provide it as parameter prepended by "@".
You can also add other explicit arguments to commands (which will overwrite those in the arguments file)

    java -jar target/dtls-fuzzer.jar @arg_file ...overwriting params...

### Batch learning
To launch a batch of learning runs, one can use the `launcher.py` script in `experiments/scripts`.
Provided a directory with argument files, the tool will launch a learning process for each argument file.

    experiments/scripts/launcher.py --jar target/dtls-fuzzer.jar --args args_folder

## Running a test suite
Before running learning experiments, it helps to check that arguments are correctly set, particularly timing parameters.
To that end, DTLS-Fuzzer can execute a custom test suite (collection of tests) on the SUT, and provide a summary of the outputs.
This functionality can also be used when diagnosing failed learning experiments, i.e. finding out what went wrong.

To run the test suite on a server using the default alphabet, you can run:

    java -jar target/dtls-fuzzer.jar -connect localhost:20000 -test test_file

For example of test files, go to `examples/tests/servers`.
A test file comprises a newline-separated list of inputs.
Tests are separated by empty new lines.
The end of each test is either the end of the file, or an empty new line.
"#" is used to comment out a line.

If you have a model/specification, you can also run the test suite and compare the output with that in a specification.

    java -jar target/dtls-fuzzer.jar -connect localhost:20000 -test test_file -specification model

The number of times tests are run is configurable by the `-times` parameter, which defaults to 1.
Setting it to a high number helps detect non-determinism in learning configurations, by comparing the output of each test.

    java -jar target/dtls-fuzzer.jar -connect localhost:20000 -test test_file -times 10

Finally, if you have the arguments file for a learning experiment, you can use them to run tests on the SUT involved by just adding the necessary test arguments:

    java -jar target/dtls-fuzzer.jar @learning_arg_file -test test_file



[tlsattacker]:https://github.com/RUB-NDS/TLS-Attacker
[tlsattackerver]:https://github.com/RUB-NDS/TLS-Attacker/releases/tag/3.0b
[graphviz]:https://www.graphviz.org
[jsse]:https://github.com/pfg666/jsse-dtls-server
[scandium]:https://github.com/pfg666/scandium-dtls-server
[piondtls]:https://github.com/pfg666/pion-dtls-server
[dottrimmer]:https://github.com/pfg666/dot-trimmer
