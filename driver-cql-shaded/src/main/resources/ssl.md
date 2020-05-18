# SSL

Supported options:

- **ssl** - specifies the type of the SSL implementation.
    Disabled by default, possible values are `jdk`, and `openssl`.

- **tlsversion** - specify the TLS version to use for SSL.

    Examples:
    - `tlsversion=TLSv1.2` (the default)

For `jdk` type, the following options are available:

- **truststore** - specify the path to the SSL truststore.

    Examples:
    - `truststore=file.truststore`

- **tspass** - specify the password for the SSL truststore.

    Examples:
    - `tspass=truststore_pass`

- **keystore** - specify the path to the SSL keystore.

    Examples:
    - `keystore=file.keystore`

- **kspass** - specify the password for the SSL keystore.

    Examples:
    - `kspass=keystore_pass`
    
- **keyPassword** - specify the password for the key.

    Examples:
    - `keyPassword=password`


For `openssl` type, the following options are available:

- **caCertFilePath** - path to the X509 CA certificate file.

    Examples:
    - `caCertFilePath=cacert.crt`
    
- **certFilePath** - path to the X509 certificate file.

    Examples:
    - `certFilePath=ca.pem`

- **keyFilePath** - path to the OpenSSL key file.

    Examples:
    - `keyFilePath=file.key`
