#!/bin/bash

if [ -z "$1" ]
then
  echo "Specify  LetsEncrypt path"
  exit 1
fi
cd $1
openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem -out jetty.p12 -CAfile chain.pem -caname root
