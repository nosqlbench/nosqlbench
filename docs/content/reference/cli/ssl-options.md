+++
title = "SSL Options"
description = "Standard SSL/TLS configuration options"
weight = 40
template = "page.html"

[extra]
quadrant = "reference"
topic = "cli"
category = "configuration"
tags = ["ssl", "tls", "security", "configuration"]
+++

These options are used when you need to configure SSL for a driver. The configuration logic for
SSL is centralized, and supports both the version of TLS which is shipped with the JVM, as well
as the openssl version.

Whenever a driver indicates that it can be configured with SSL, and points you to
the _standard SSL options_, this is the page it is referring to.

# SSL Options

## ssl

Specifies the type of the SSL implementation.

Disabled by default, possible values are `jdk`, and `openssl`.

**examples**

- `ssl=jdk`
- `ssl-openssl`

The options available depend on which of these you choose. See the relevant sections below.

# with ssl=jdk

## tlsversion

Specifies the TLS version to use for SSL.

**examples**

- `tlsversion=TLSv1.2` (the default)

## truststore

Specifies the path to the SSL truststore.

**examples**

- `truststore=file.truststore`

## tspass

Specifies the password for the SSL truststore.

**examples**

- `tspass=truststore_pass`

## keystore

Specifies the path to the SSL keystore.

**examples**

- `keystore=file.keystore`

## kspass

Specifies the password for the SSL keystore.

**examples**

- `kspass=keystore_pass`

## keyPassword

Specifies the password for the key.

**examples**

- `keyPassword=password`

# with ssl=openssl

For `openssl` type, the following options are available:

## caCertFilePath

The path to the X509 CA certificate file.

**examples**

- `caCertFilePath=cacert.crt`

## certFilePath

The path to the X509 certificate file.

**examples**

- `certFilePath=ca.pem`

## keyFilePath

The path to the OpenSSL key file.

**examples**

- `keyFilePath=file.key`
