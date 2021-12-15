# Bug Patterns
Bug patterns are modeled as DFAs which only accept traces which exhibit bugs.
These, intersected with a DFA translation of the SUT model, give us the bugs in the system. 
Unlike the learner-generated DTLS Mealy machines, which have input and output alphabets, DFAs only have labels.
Hence, for each input/output in the Mealy machine, there is a corresponding input/output label that can be used in the pattern specification.
Bug pattern specification is further eased by special constructs which we will get to shortly. 

The following conventions should be followed:

* input labels start with '?' (e.g. ?HELLO_VERIFY_REQUEST);
* output labels start with '!' (e.g. !SERVER_HELLO);
* 'OTHER' from a state refers to the set of all input/output labels for which no outgoing transition has been specified;
* 'OTHER_INPUT' and 'OTHER_OUTPUT' are like 'OTHER', but refer to either inputs or outputs;
* regex labels are of the form '?$regex' for inputs, and '!$regex' for outputs, (e.g. ?$.*CLIENT_HELLO) and refer to the set of all inputs/outputs in the model that match the regular expression 'regex'. 


We chose regular expressions since they are expressive enough to fit out purpose, and well documented.
To process regular expressions, we use Java's Pattern class. 
Check its [online documentation](https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html) to see how to formulate regular expressions.

Commonly used constructs in specifications are:

- **any** '.*', e.g. '?$.*_CLIENT_HELLO' meaning any CLIENT_HELLO input;
- **or** '|', e.g '!$SERVER_HELLO|CERTIFICATE_REQUEST' meaning either a SERVER_HELLO or a CERTIFICATE_REQUEST output;
- **negative lookahead** '?!', e.g. '!$(?!SERVER_HELLO\\\\Z).*' meaning anything other than a SERVER_HELLO output, where \\\\Z marks the end of the word (otherwise !SERVER_HELLO_DONE would also match the pattern);
- **capture groups** '()', e.g. ?$(.*)_CLIENT_HELLO, which matches to any CLIENT_HELLO input (e.g. PSK_CLIENT_HELLO), and additionally, captures part of this input in a capture group (PSK).

When processing regex labels, we store the matching contents of capture groups in registers (the contents of the first capture group is stored in register 1, the second in register 2...). 
The capture groups can be referred to in other inputs in the model. using a construct similar to back-references (\1 refers to the first register, \2 to the second...).
These references are resolved before processing a each regular expression.
For example, if register 1 contains 'PSK', when processing ?$\1_CLIENT_HELLO, we first resolve the reference, resulting in ?$PSK_CLIENT_HELLO, and only then handle the regular expression.
Capture groups are sometimes used to express more complex bug patterns.


