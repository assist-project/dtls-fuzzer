Here we have patches that should be applied on the SUTs prior to testing.

These patches mainly tweak timing parameters, so as to increase the receive timeout value for DTLS.
This greatly reduces the likelihood of retransmissions while executing long queries/tests. 
Retransmissions are the main cause for failure in learning.

Other patches configure the PSK of the implementations so that it matches the one we use. 
