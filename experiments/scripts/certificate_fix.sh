#!/usr/bin/env bash
#
# Since the USENIX work the mapper received some updates.
# In particular, the CERTIFICATE output now encodes the type of the certificate.
# This script partially fixes older models, so that they are consistent with the new mapper.

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
readonly SCRIPT_DIR

sed -i 's/SERVER_HELLO\,CERTIFICATE/SERVER_HELLO\,RSA_CERTIFICATE/g' "${SCRIPT_DIR}"/models/servers/*/model.dot
