# CQL Load Balancing Options

WIth the CQL driver, you may configure the load balancing with the same options you might use in
client code. However, they are expressed here in a command-line friendly form.

## Combining Policies

To apply these load balancer policies, set the activity parameter `lbp` with a comma-separated list
of policies from the examples below.

They are build as a nested set of polices, with the semantics of "and then". For example, the
TokenAwarePolicy followed by the LatencyAwarePolicy looks like `TAP(...),LAP(...)` which means
`TokenAwarePolicy(...)` and then `LatencyAwarePolicy(...)`. This is equivalent to Java code which
first constructs a LatencyAwarePolicy and then wraps it with a TokenAwarePolicy. This follows the
notion that the outer-most policy has primary control over options presented to child policies, and
thus you can think of the routing process as "TokenAwarePolicy decides ... " *and then* with what it
shares with the wrapped child policy, "LatencyAwarePolicy decides...", and so on.

Even though you can use the simple pollicy descriptions above, they are constructed in the same
programmatic way in Java that you would use to nest them in the specified order.

For example, a token aware policy wrapping a white list policy might look like this on your command
line:

    lbp=TAP(),WLP(127.0.0.1)

## Supported Load Balancer Policies

Each supported policy is described in detail below, with the options supported.

### RRP: Round Robin Policy

Format: `RRP()`

**note** You can't wrap another policy with RRP.

### WLP: White List Policy

Format: `WLP(addr,...)`

### TAP: Token Aware Policy

Format: `TAP()`

### LAP: Latency Aware Policy

This policy has many optional parameters, so if you use it you must set them by name.

Format: `LAP(options...)`, where each option is one of the following:

- `exclusion_threshold` (or `et`) - The exclusion threshold, or how much worse a node has to be to
  be excluded for awhile. Javadoc: The default exclusion threshold (if this method is not called) is
  `2`. In other words, the resulting policy excludes nodes that are more than twice slower than the
  fastest node.
- `minimum_measurements` (or `mm`) - The minimum number of measurements to take before penalizing a
  host. Javadoc: The default for this option (if this method is not called) is `50`. Note that it is
  probably not a good idea to put this option too low if only to avoid the influence of JVM warm-up
  on newly restarted nodes.
- `retry_period` (or `rp`) - The retry period, in seconds. Javadoc: The retry period defines how
  long a node may be penalized by the policy before it is given a 2nd chance. This is 10 seconds by
  default.
- `retry_period_ms` (or `rp_ms`) - The retry period, in milliseconds. This is the same as above, but
  allows you to have more precise control if needed.
- `scale` (or `s`) - The scale parameter adjusts how abruptly the most recent measurements are
  scaled down in the moving average over time. 100ms is the default. Higher values reduce the
  significance of more recent measurements, lower values increase it. The default is 100ms.
- `scale_ms` - The scale parameter, in milliseconds. This is the same as above, but allows you to
  have more prcise control if needed.
- `update_rate` (or `ur`) - How often a node's latency average is computed. The default is 1/10
  second.
- `update_rate_ms` (or `ur_ms`) - The update rate, in milliseconds.

Examples:
- `lbp="LAP(mm=10,rp_ms=10000)"`
- `lbp="LatencyAwarePolicy(minimum_measurements=10,retry_period_ms=10000)"`

### DCARRP: DC-Aware Round Robin Policy

Format: `DCARRP(localdc=somedcname)`

This load balancing policy does not expose other non-deprecated options in the bundled version of
the driver, and the datacenter name is required.

