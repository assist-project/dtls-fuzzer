# Caution
This document is meant to be consistent with the current state of the project.
It may not apply to older versions of the tool.

# Implementations
The JSSE server is at:
https://github.com/pfg666/jsse-dtls-server

The SCANDIUM client/server are at:
https://github.com/assist-project/scandium-dtls-examples

The PionDTLS client/server are at:
https://github.com/assist-project/pion-dtls-examples

# Versions/Commits


# Troubleshooting

## GnuTLS
This library can be a pain to get working due to external dependencies, and inconsistencies in how environmental variables are set.
If the setup_sut.sh script is not working, the following steps can be taken:

1. Manually install all the dependencies, including pkgconfig.


2. Set $PKG_CONFIG_PATH in ~/.bashrc to a directory which contains nettle.pc and hogweed.pc. For example:

PKG_CONFIG_PATH=$PKG_CONFIG_PATH:/usr/local/lib64/pkgconfig/

3. Ensure $LD_LIBRARY_PATH, variable referring to libraries loaded at runtime, also refers to the directory containing libnettle.so and libhogweed.so. 
This can be done in two ways, either set LD_LIBRARY_PATH in the command used to launch the SUT:

LD_LIBRARY_PATH=/usr/local/lib64 suts/gnutls/src/gnutls-cli

Or include the directory in /etc/ld.so.config.d/*.conf, files which contain paths to all the directories whose libraries are loaded at runtime.

Then run:

sudo ldconfig



