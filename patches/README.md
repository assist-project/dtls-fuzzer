Here we have patches that should be applied on the SUTs so that learning is more likely to work.
These patches mainly tweak timing parameters, so as to increase the receive timeout value for DTLS.
This greatly reduces the likelihood of retransmissions while executing long queries/tests.