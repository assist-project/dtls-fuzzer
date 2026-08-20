#! /bin/env sh

export NIXPKGS_ALLOW_INSECURE=1
nix-shell --impure -p python2 jdk21 automake autoconf libtool gettext m4
