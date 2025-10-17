# Baselines/http-rest-* Usage Guide

Identifies some key settings, as well as tips & tricks for use with the `http-rest-*` baseline activities.

## Hosts
### weighted_hosts
Enabling an optional weighted set of hosts in place of a load balancer.

### Examples
* single host: restapi_host=host1
* multiple hosts: restapi_host=host1,host2,host3
* multiple weighted hosts: restapi_host=host1:3,host2:7

## Tokens
Currently supporting Stargate, with plans to make more generic where REST tokens are required.  Provide your own auth token via param `auth_token` in which case, the following 'auto_gen_token' 
can be commented out.  Otherwise, the default automatic token generation is used.

### auto_gen_token
Use the `Token('TEMPLATE(auth_token,)','TEMPLATE(auth_uri,http://localhost:8081/v1/auth)', 'TEMPLATE(auth_uid,cassandra)', 'TEMPLATE(auth_pswd,cassandra)');` string binding function, which auto-generates, caches, and returns a token with automatic refresh upon expiry.
When the `auth_token` is provided only that token value will be used (i.e. not auto-generated) and the rest of the passed in values for uri, uid, and pswd are ignored.

### Example using all defaults.
token=Discard(); Token('TEMPLATE(auth_token,)','TEMPLATE(auth_uri,http://localhost:8081/v1/auth)', 'TEMPLATE(auth_uid,cassandra)', 'TEMPLATE(auth_pswd,cassandra)');

Here `Discard()` will ignore the long input value received (by default for the binding), then the Token function automates a check for a cached tokens from the endpoint provided.
If not already discovered, a request to the local running token service will be invoked.
Each subsequent cycles utilizes the cached value until a timeout occurs (default 30m) or the workload is restarted.

Regardless of the option used, both will be considered to determine the value for the following:
```yaml
X-Cassandra-Token: "{token}"
```
