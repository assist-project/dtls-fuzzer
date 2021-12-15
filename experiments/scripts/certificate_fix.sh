# Since the USENIX work the mapper received some updates, in particular, the CERTIFICATE output now encodes the type of the certificate
# This script partially fixes older models, so that they are consistent with the new mapper.

readonly SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

sed -i 's/SERVER_HELLO\,CERTIFICATE/SERVER_HELLO\,RSA_CERTIFICATE/g' $SCRIPT_DIR/models/servers/*/model.dot
