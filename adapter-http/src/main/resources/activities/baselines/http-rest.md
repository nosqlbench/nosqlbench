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
Use the `StargateToken('url')` unary string binding function, which auto-generates, caches, and returns a token with automatic refresh upon expiry.

### Example
* auto_gen_token: Discard(); StargateToken('http://localhost:8081/v1/auth');

Here `Discard()` will ignore the long input value received (by default for the binding), then the StargateToken function automates a check for a cached Stargate token.  
If not already discovered, a request to the local running Stargate service will be invoked.  Each subsequent cycles utilizes the cached value until a timeout occurs or the workload is restarted.

Regardless of the option used, both will be considered to determine the value for the following:
```yaml
X-Cassandra-Token: "<<{auth_token}:{auto_gen_token}>>"
```
