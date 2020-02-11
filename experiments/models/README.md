This folder contains the models generated.

 -  'raw' contains unedited models
 -  'trimmed' contains models which have been processed using scripts
 -  'redacted' contains models which are the result of script processing and some manual editing to highlight non-conforming behavior
    - for these we also provide .pdf versions generated via the graphviz library

Model names suggest the implementation, alphabet in terms of key exchange algorithms, where applicable, the client authentication setting used, optionally whether retransmissions were included (*incl*) or excluded (*excl*), and whether states from which a handshake could not be completed were removed (*reduced*).
 
The scripts used compactify models, color handshake paths, and for big models, remove states from which a handshake cannot be completed.