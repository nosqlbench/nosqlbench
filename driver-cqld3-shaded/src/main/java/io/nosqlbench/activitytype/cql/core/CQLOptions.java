package io.nosqlbench.activitytype.cql.core;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.*;
import io.netty.util.HashedWheelTimer;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CQLOptions {
    private final static Logger logger = LogManager.getLogger(CQLOptions.class);

    private final static Pattern CORE_AND_MAX_RQ_PATTERN = Pattern.compile("(?<core>\\d+)(:(?<max>\\d+)(:(?<rq>\\d+))?)?(,(?<rcore>\\d+)(:(?<rmax>\\d+)(:(?<rrq>\\d+))?)?)?(,?heartbeat_interval_s:(?<heartbeatinterval>\\d+))?(,?idle_timeout_s:(?<idletimeout>\\d+))?(,?pool_timeout_ms:(?<pooltimeout>\\d+))?");
    private final static Pattern PERCENTILE_EAGER_PATTERN = Pattern.compile("^p(?<pctile>[^:]+)(:(?<executions>\\d+))?(:(?<tracked>\\d+)ms)?$");
    private final static Pattern CONSTANT_EAGER_PATTERN = Pattern.compile("^((?<msThreshold>\\d++)ms)(:(?<executions>\\d+))?$");

    private static ConstantSpeculativeExecutionPolicy constantPolicy(int threshold, int executions) {
        return new ConstantSpeculativeExecutionPolicy(threshold, executions);
    }

    private static SpeculativeExecutionPolicy percentilePolicy(long tracked, double threshold, int executions) {
        PerHostPercentileTracker tracker = newTracker(tracked);
        return new PercentileSpeculativeExecutionPolicy(tracker, threshold, executions);
    }

    private static PerHostPercentileTracker newTracker(long millis) {
        return PerHostPercentileTracker.builder(millis).build();
    }

    public static PoolingOptions poolingOptionsFor(String spec) {
        Matcher matcher = CORE_AND_MAX_RQ_PATTERN.matcher(spec);
        if (matcher.matches()) {
            PoolingOptions poolingOptions = new PoolingOptions();

            Optional.ofNullable(matcher.group("core")).map(Integer::valueOf)
                    .ifPresent(core -> poolingOptions.setCoreConnectionsPerHost(HostDistance.LOCAL, core));
            Optional.ofNullable(matcher.group("max")).map(Integer::valueOf)
                    .ifPresent(max -> poolingOptions.setMaxConnectionsPerHost(HostDistance.LOCAL, max));
            Optional.ofNullable(matcher.group("rq")).map(Integer::valueOf)
                    .ifPresent(rq -> poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL, rq));

            Optional.ofNullable(matcher.group("rcore")).map(Integer::valueOf)
                    .ifPresent(rcore -> poolingOptions.setCoreConnectionsPerHost(HostDistance.REMOTE, rcore));
            Optional.ofNullable(matcher.group("rmax")).map(Integer::valueOf)
                    .ifPresent(rmax -> poolingOptions.setMaxConnectionsPerHost(HostDistance.REMOTE, rmax));
            Optional.ofNullable(matcher.group("rrq")).map(Integer::valueOf)
                    .ifPresent(rrq -> poolingOptions.setMaxRequestsPerConnection(HostDistance.REMOTE, rrq));

            Optional.ofNullable(matcher.group("heartbeatinterval")).map(Integer::valueOf)
                    .ifPresent(poolingOptions::setHeartbeatIntervalSeconds);

            Optional.ofNullable(matcher.group("idletimeout")).map(Integer::valueOf)
                    .ifPresent(poolingOptions::setIdleTimeoutSeconds);

            Optional.ofNullable(matcher.group("pooltimeout")).map(Integer::valueOf)
                    .ifPresent(poolingOptions::setPoolTimeoutMillis);

            return poolingOptions;
        }
        throw new RuntimeException("No pooling options could be parsed from spec: " + spec);

    }

    public static RetryPolicy retryPolicyFor(String spec) {
        Set<String> retryBehaviors = Arrays.stream(spec.split(",")).map(String::toLowerCase).collect(Collectors.toSet());
        RetryPolicy retryPolicy = DefaultRetryPolicy.INSTANCE;

        if (retryBehaviors.contains("default")) {
            return retryPolicy;
        } // add other mutually-exclusive behaviors here with checks, if we want to extend beyond "default"

        if (retryBehaviors.contains("logging")) {
            retryPolicy = new LoggingRetryPolicy(retryPolicy);
        }

        return retryPolicy;
    }

    public static ReconnectionPolicy reconnectPolicyFor(String spec) {
        if (spec.startsWith("exponential(")) {
            String argsString = spec.substring(12);
            String[] args = argsString.substring(0, argsString.length() - 1).split("[,;]");
            if (args.length != 2) {
                throw new BasicError("Invalid reconnectionpolicy, try reconnectionpolicy=exponential(<baseDelay>, <maxDelay>)");
            }
            long baseDelay = Long.parseLong(args[0]);
            long maxDelay = Long.parseLong(args[1]);
            return new ExponentialReconnectionPolicy(baseDelay, maxDelay);
        } else if (spec.startsWith("constant(")) {
            String argsString = spec.substring(9);
            long constantDelayMs = Long.parseLong(argsString.substring(0, argsString.length() - 1));
            return new ConstantReconnectionPolicy(constantDelayMs);
        }
        throw new BasicError("Invalid reconnectionpolicy, try reconnectionpolicy=exponential(<baseDelay>, <maxDelay>) or constant(<constantDelayMs>)");
    }

    public static SocketOptions socketOptionsFor(String spec) {
        String[] assignments = spec.split("[,;]");
        Map<String, String> values = new HashMap<>();
        for (String assignment : assignments) {
            String[] namevalue = assignment.split("[:=]", 2);
            String name = namevalue[0];
            String value = namevalue[1];
            values.put(name, value);
        }

        SocketOptions options = new SocketOptions();
        Optional.ofNullable(values.get("read_timeout_ms")).map(Integer::parseInt).ifPresent(
                options::setReadTimeoutMillis
        );
        Optional.ofNullable(values.get("connect_timeout_ms")).map(Integer::parseInt).ifPresent(
                options::setConnectTimeoutMillis
        );
        Optional.ofNullable(values.get("keep_alive")).map(Boolean::parseBoolean).ifPresent(
                options::setKeepAlive
        );
        Optional.ofNullable(values.get("reuse_address")).map(Boolean::parseBoolean).ifPresent(
                options::setReuseAddress
        );
        Optional.ofNullable(values.get("so_linger")).map(Integer::parseInt).ifPresent(
                options::setSoLinger
        );
        Optional.ofNullable(values.get("tcp_no_delay")).map(Boolean::parseBoolean).ifPresent(
                options::setTcpNoDelay
        );
        Optional.ofNullable(values.get("receive_buffer_size")).map(Integer::parseInt).ifPresent(
                options::setReceiveBufferSize
        );
        Optional.ofNullable(values.get("send_buffer_size")).map(Integer::parseInt).ifPresent(
                options::setSendBufferSize
        );

        return options;
    }

    public static SpeculativeExecutionPolicy defaultSpeculativePolicy() {
        PerHostPercentileTracker tracker = PerHostPercentileTracker
                .builder(15000)
                .build();
        PercentileSpeculativeExecutionPolicy defaultSpecPolicy =
                new PercentileSpeculativeExecutionPolicy(tracker, 99.0, 5);
        return defaultSpecPolicy;
    }

    public static SpeculativeExecutionPolicy speculativeFor(String spec) {
        Matcher pctileMatcher = PERCENTILE_EAGER_PATTERN.matcher(spec);
        Matcher constantMatcher = CONSTANT_EAGER_PATTERN.matcher(spec);
        if (spec.toLowerCase().trim().matches("disabled|none")) {
            return null;
        } else if (spec.toLowerCase().trim().equals("default")) {
            return defaultSpeculativePolicy();
        } else if (pctileMatcher.matches()) {
            double pctile = Double.valueOf(pctileMatcher.group("pctile"));
            if (pctile > 100.0 || pctile < 0.0) {
                throw new RuntimeException("pctile must be between 0.0 and 100.0");
            }
            String executionsSpec = pctileMatcher.group("executions");
            String trackedSpec = pctileMatcher.group("tracked");
            int executions = (executionsSpec != null && !executionsSpec.isEmpty()) ? Integer.valueOf(executionsSpec) : 5;
            int tracked = (trackedSpec != null && !trackedSpec.isEmpty()) ? Integer.valueOf(trackedSpec) : 15000;
            logger.debug("speculative: Creating new percentile tracker policy from spec '" + spec + "'");
            return percentilePolicy(tracked, pctile, executions);
        } else if (constantMatcher.matches()) {
            int threshold = Integer.valueOf(constantMatcher.group("msThreshold"));
            String executionsSpec = constantMatcher.group("executions");
            int executions = (executionsSpec != null && !executionsSpec.isEmpty()) ? Integer.valueOf(executionsSpec) : 5;
            logger.debug("speculative: Creating new constant policy from spec '" + spec + "'");
            return constantPolicy(threshold, executions);
        } else {
            throw new RuntimeException("Unable to parse pattern for speculative option: " + spec + ", it must be in " +
                    "an accepted form, like p99.0:5:15000, or p99.0:5, or 5000ms:5");
        }

    }

    public static LoadBalancingPolicy whitelistFor(String s, LoadBalancingPolicy innerPolicy) {
        String[] addrSpecs = s.split(",");
        List<InetSocketAddress> sockAddrs = Arrays.stream(addrSpecs)
                .map(CQLOptions::toSocketAddr)
                .collect(Collectors.toList());
        if (innerPolicy == null) {
            innerPolicy = new RoundRobinPolicy();
        }
        return new WhiteListPolicy(innerPolicy, sockAddrs);
    }

    public static LoadBalancingPolicy lbpolicyFor(String polspec, LoadBalancingPolicy policy) {
        Pattern polcall = Pattern.compile(",?(?<policyname>\\w+)\\((?<args>[^)]+)?\\)");
        Matcher matcher = polcall.matcher(polspec);
        Deque<List<String>> policies = new ArrayDeque<>();

        while (matcher.find()) {
            String policyname = matcher.group("policyname");
            String argsgroup = matcher.group("args");
            String args = argsgroup==null ? "" : argsgroup;
            logger.debug("policyname=" + policyname);
            logger.debug("args=" + args);
            policies.push(List.of(policyname,args));
        }

        // reverse order for proper nesting
        while (policies.size()>0) {
            List<String> nextpolicy = policies.pop();
            String policyname = nextpolicy.get(0)
                    .replaceAll("_", "")
                    .replaceAll("policy", "");
            String argslist = nextpolicy.get(1);
            String[] args= argslist.isBlank() ? new String[0] : argslist.split(",");

            switch (policyname) {
                case "WLP":
                case "whitelist":
                    List<InetSocketAddress> sockAddrs = Arrays.stream(args)
                            .map(CQLOptions::toSocketAddr)
                            .collect(Collectors.toList());
                    policy = new WhiteListPolicy(policy, sockAddrs);
                    break;
                case "TAP":
                case "tokenaware":
                    TokenAwarePolicy.ReplicaOrdering ordering = TokenAwarePolicy.ReplicaOrdering.NEUTRAL;
                    if (args.length==1) {
                        if (args[0].startsWith("ordering=") || args[0].startsWith("ordering:")) {
                            String orderingSpec = args[0].substring("ordering=".length()).toUpperCase();
                            ordering=TokenAwarePolicy.ReplicaOrdering.valueOf(orderingSpec);
                        } else {
                            throw new BasicError("Unrecognized option for " + TokenAwarePolicy.class.getCanonicalName());
                        }
                    }
                    policy = new TokenAwarePolicy(policy, ordering);
                    break;
                case "LAP":
                case "latencyaware":
                    policy = latencyAwarePolicyFor(args,policy);
                    break;
                case "DCARRP":
                case "dcawareroundrobin":
                case "datacenterawareroundrobin":
                    if (policy!=null) {
                        throw new BasicError(DCAwareRoundRobinPolicy.class.getCanonicalName() + " can not wrap another policy.");
                    }
                    policy = dcAwareRoundRobinPolicyFor(args);
                    break;
                default:
                    throw new BasicError("Unrecognized policy selector '" + policyname + "', please select one of WLP,TAP,LAP,DCARRP, or " +
                            "one of whitelist, tokenaware, latencyaware, dcawareroundrobin.");
            }
        }
        return policy;
    }

    private static LoadBalancingPolicy dcAwareRoundRobinPolicyFor(String[] args) {
        if (args.length==0){
            throw new BasicError(DCAwareRoundRobinPolicy.class.getCanonicalName() + " requires a local DC name.");
        }
        DCAwareRoundRobinPolicy.Builder builder = DCAwareRoundRobinPolicy.builder();
        for (String arg : args) {
            String[] kv = arg.split("[:=]", 2);
            if (kv.length != 2) {
                throw new BasicError("LatencyAwarePolicy specifier requires named parameters like `exclusion_threshold=23.0`");
            }
            switch(kv[0]) {
                case "local":
                case "localdc":
                    builder.withLocalDc(kv[1]);
                    break;
                default:
                    throw new BasicError("Unknown option for " + DCAwareRoundRobinPolicy.class.getSimpleName() + ": '" + kv[0] + "'");
            }
        }
        return builder.build();
    }

    private static LoadBalancingPolicy latencyAwarePolicyFor(String[] args, LoadBalancingPolicy childPolicy) {
        LatencyAwarePolicy.Builder builder = LatencyAwarePolicy.builder(childPolicy);

        for (String arg : args) {
            String[] kv = arg.split("[:=]", 2);
            if (kv.length != 2) {
                throw new BasicError("LatencyAwarePolicy specifier requires named parameters like `exclusion_threshold=23.0`");
            }

            switch (kv[0]) {
                case "exclusion_threshold":
                case "et":
                    builder = builder.withExclusionThreshold(Double.parseDouble(kv[1]));
                    break;

                case "minimum_measurements":
                case "mm":
                    builder = builder.withMininumMeasurements(Integer.parseInt(kv[1]));
                    break;

                case "retry_period_ms":
                case "rp_ms":
                    builder = builder.withRetryPeriod(Long.parseLong(kv[1]), TimeUnit.MILLISECONDS);
                    break;

                case "retry_period":
                case "rp":
                    builder = builder.withRetryPeriod(Long.parseLong(kv[1]), TimeUnit.SECONDS);
                    break;

                case "scale":
                case "s":
                    builder = builder.withScale(Long.parseLong(kv[1]), TimeUnit.SECONDS);
                    break;

                case "scale_ms":
                case "s_ms":
                    builder = builder.withScale(Long.parseLong(kv[1]), TimeUnit.MILLISECONDS);
                    break;

                case "update_rate":
                case "ur":
                    builder.withUpdateRate(Long.parseLong(kv[1]), TimeUnit.SECONDS);
                    break;
                case "update_rate_ms":
                case "ur_ms":
                    builder.withUpdateRate(Long.parseLong(kv[1]), TimeUnit.MILLISECONDS);
                    break;
            }

        }
        return builder.build();
    }

    public static NettyOptions withTickDuration(String tick) {
        logger.info("Cluster builder using custom tick duration value for HashedWheelTimer: " + tick + " milliseconds");
        int tickDuration = Integer.valueOf(tick);
        return new NettyOptions() {
            public io.netty.util.Timer timer(ThreadFactory threadFactory) {
                return new HashedWheelTimer(
                        threadFactory, tickDuration, TimeUnit.MILLISECONDS);
            }
        };
    }

    private static InetSocketAddress toSocketAddr(String addr) {
        String[] addrs = addr.split(":", 2);
        String inetHost = addrs[0];
        String inetPort = (addrs.length == 2) ? addrs[1] : "9042";
        return new InetSocketAddress(inetHost, Integer.valueOf(inetPort));
    }

    public static ProtocolOptions.Compression withCompression(String compspec) {
        try {
            return ProtocolOptions.Compression.valueOf(compspec);
        } catch (IllegalArgumentException iae) {
            throw new RuntimeException("Compression option '" + compspec + "' was specified, but only " +
                    Arrays.toString(ProtocolOptions.Compression.values()) + " are available.");
        }
    }
}
