By default, DTLS-Fuzzer uses as configuration the `sul.config` file.
It is best if that configuration file is used, and we have as little variation as possible in the setup.
However, sometimes that is not possible: some SUT's really need specifically tailored settings.

# Notable adjustments for state fuzzing

## Disabling ExtendedMasterSecretExtension
This extension requires that a hash over the message digest is included in the pre-master computation/key generation.
While this makes sense for security, it can hinder state fuzzing since it will make it more likely for the fuzzer the generate a different set of keys compared to the SUT.
This leads to cases where received records cannot be decrypted.
Note that enabling this extension for SUTs that do not support it should have no effect.


# Adjustments for different SUTS

## GnuTLS
GnuTLS demands that the PSK password be longer than 4, the length of the password used by default.
As a result, we had to make a separate configuration file for it.

## TinyDTLS
TinyDTLS uses raw keys, requiring a special configuration file in which the EC keys are specified.
TinyDTLS is also sensitive to extensions it does not recognize, with the default reaction being to abort the connection.
Hence, a few more extensions were disabled.

## Scandium
Scandium supports only EC Certificates.
