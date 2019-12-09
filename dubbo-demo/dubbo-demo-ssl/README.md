Procedures for generating sample credentials (with self-signed CA), also check [here](https://phoenixnap.com/kb/openssl-tutorial-ssl-certificates-private-keys-csrs?from=timeline&isappinstalled=0) 
for how to generate credentials without CA.

## 1. Generate self-signed CA

$ openssl req -x509 -new -newkey rsa:1024 -keyout ca.key -nodes -out ca.pem -config -days 3650 -extensions v3_req
When prompted for certificate information, everything is default.

## 2. client is issued by CA:

$ openssl genrsa -out client.key.rsa 1024
$ openssl pkcs8 -topk8 -in client.key.rsa -out client.key -nocrypt
$ rm client.key.rsa
$ openssl req -new -key client.key -out client.csr

When prompted for certificate information, everything is default except the
common name which is set to testclient.

$ openssl ca -in client.csr -out client.pem -keyfile ca.key -cert ca.pem -verbose -days 3650 -updatedb
$ openssl x509 -in client.pem -out client.pem -outform PEM

## 3. server0 is issued by CA:

$ openssl genrsa -out server0.key.rsa 1024
$ openssl pkcs8 -topk8 -in server0.key.rsa -out server0.key -nocrypt
$ rm server0.key.rsa
$ openssl req -new -key server0.key -out server0.csr

When prompted for certificate information, everything is default except the
common name which is set to *.test.google.com.au.

$ openssl ca -in server0.csr -out server0.pem -keyfile ca.key -cert ca.pem -verbose -config openssl.cnf -days 3650 -updatedb
$ openssl x509 -in server0.pem -out server0.pem -outform PEM

server1 is issued by CA with a special config for subject alternative names:
----------------------------------------------------------------------------

$ openssl genrsa -out server1.key.rsa 1024
$ openssl pkcs8 -topk8 -in server1.key.rsa -out server1.key -nocrypt
$ rm server1.key.rsa
$ openssl req -new -key server1.key -out server1.csr -config server1-openssl.cnf

When prompted for certificate information, everything is default except the
common name which is set to *.test.google.com.

$ openssl ca -in server1.csr -out server1.pem -keyfile ca.key -cert ca.pem -verbose -config server1-openssl.cnf -days 3650 -extensions v3_req -updatedb
$ openssl x509 -in server1.pem -out server1.pem -outform PEM

Gotchas
=======

You may have to delete and recreate the index.txt file so that it is empty when
running the `openssl ca` command.