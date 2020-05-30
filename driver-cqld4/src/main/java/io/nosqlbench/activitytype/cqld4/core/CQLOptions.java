package io.nosqlbench.activitytype.cqld4.core;

public class CQLOptions {
//    private final static Logger logger = LoggerFactory.getLogger(CQLOptions.class);
//
//    private final static Pattern CORE_AND_MAX_RQ_PATTERN = Pattern.compile("(?<core>\\d+)(:(?<max>\\d+)(:(?<rq>\\d+))?)?(,(?<rcore>\\d+)(:(?<rmax>\\d+)(:(?<rrq>\\d+))?)?)?(,?heartbeat_interval_s:(?<heartbeatinterval>\\d+))?(,?idle_timeout_s:(?<idletimeout>\\d+))?(,?pool_timeout_ms:(?<pooltimeout>\\d+))?");
//    private final static Pattern PERCENTILE_EAGER_PATTERN = Pattern.compile("^p(?<pctile>[^:]+)(:(?<executions>\\d+))?(:(?<tracked>\\d+)ms)?$");
//    private final static Pattern CONSTANT_EAGER_PATTERN = Pattern.compile("^((?<msThreshold>\\d++)ms)(:(?<executions>\\d+))?$");
//
//    private static ConstantSpeculativeExecutionPolicy constantPolicy(DriverContext context, int threshold, int executions) {
//        return new ConstantSpeculativeExecutionPolicy(threshold, executions);
//    }
//
//    private static SpeculativeExecutionPolicy percentilePolicy(long tracked, double threshold, int executions) {
//        PerHostPercentileTracker tracker = newTracker(tracked);
//        return new PercentileSpeculativeExecutionPolicy(tracker, threshold, executions);
//    }
//
//    private static PerHostPercentileTracker newTracker(long millis) {
//        return PerHostPercentileTracker.builder(millis).build();
//    }
//
//    public static PoolingOptions poolingOptionsFor(String spec) {
//        Matcher matcher = CORE_AND_MAX_RQ_PATTERN.matcher(spec);
//        if (matcher.matches()) {
//            PoolingOptions poolingOptions = new PoolingOptions();
//
//            Optional.ofNullable(matcher.group("core")).map(Integer::valueOf)
//                    .ifPresent(core -> poolingOptions.setCoreConnectionsPerHost(HostDistance.LOCAL, core));
//            Optional.ofNullable(matcher.group("max")).map(Integer::valueOf)
//                    .ifPresent(max -> poolingOptions.setMaxConnectionsPerHost(HostDistance.LOCAL, max));
//            Optional.ofNullable(matcher.group("rq")).map(Integer::valueOf)
//                    .ifPresent(rq -> poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL, rq));
//
//            Optional.ofNullable(matcher.group("rcore")).map(Integer::valueOf)
//                    .ifPresent(rcore -> poolingOptions.setCoreConnectionsPerHost(HostDistance.REMOTE, rcore));
//            Optional.ofNullable(matcher.group("rmax")).map(Integer::valueOf)
//                    .ifPresent(rmax -> poolingOptions.setMaxConnectionsPerHost(HostDistance.REMOTE, rmax));
//            Optional.ofNullable(matcher.group("rrq")).map(Integer::valueOf)
//                    .ifPresent(rrq -> poolingOptions.setMaxRequestsPerConnection(HostDistance.REMOTE, rrq));
//
//            Optional.ofNullable(matcher.group("heartbeatinterval")).map(Integer::valueOf)
//                    .ifPresent(poolingOptions::setHeartbeatIntervalSeconds);
//
//            Optional.ofNullable(matcher.group("idletimeout")).map(Integer::valueOf)
//                    .ifPresent(poolingOptions::setIdleTimeoutSeconds);
//
//            Optional.ofNullable(matcher.group("pooltimeout")).map(Integer::valueOf)
//                    .ifPresent(poolingOptions::setPoolTimeoutMillis);
//
//            return poolingOptions;
//        }
//        throw new RuntimeException("No pooling options could be parsed from spec: " + spec);
//
//    }
//
//    public static RetryPolicy retryPolicyFor(String spec, Session session) {
//        Set<String> retryBehaviors = Arrays.stream(spec.split(",")).map(String::toLowerCase).collect(Collectors.toSet());
//        RetryPolicy retryPolicy = new DefaultRetryPolicy(session.getContext(),"default");
//
//        if (retryBehaviors.contains("default")) {
//            return retryPolicy;
//        } // add other mutually-exclusive behaviors here with checks, if we want to extend beyond "default"
//
//        if (retryBehaviors.contains("logging")) {
//            retryPolicy = new LoggingRetryPolicy(retryPolicy);
//        }
//
//        return retryPolicy;
//    }
//
//    public static ReconnectionPolicy reconnectPolicyFor(String spec, Session session) {
//       if(spec.startsWith("exponential(")){
//           String argsString = spec.substring(12);
//           String[] args = argsString.substring(0, argsString.length() - 1).split("[,;]");
//           if (args.length != 2){
//               throw new BasicError("Invalid reconnectionpolicy, try reconnectionpolicy=exponential(<baseDelay>, <maxDelay>)");
//           }
//           long baseDelay = Long.parseLong(args[0]);
//           long maxDelay = Long.parseLong(args[1]);
//           ExponentialReconnectionPolicy exponentialReconnectionPolicy = new ExponentialReconnectionPolicy(session.getContext());
//       }else if(spec.startsWith("constant(")){
//           String argsString = spec.substring(9);
//           long constantDelayMs= Long.parseLong(argsString.substring(0, argsString.length() - 1));
//           return new ConstantReconnectionPolicy(constantDelayMs);
//       }
//       throw new BasicError("Invalid reconnectionpolicy, try reconnectionpolicy=exponential(<baseDelay>, <maxDelay>) or constant(<constantDelayMs>)");
//    }
//
//    public static SocketOptions socketOptionsFor(String spec) {
//        String[] assignments = spec.split("[,;]");
//        Map<String, String> values = new HashMap<>();
//        for (String assignment : assignments) {
//            String[] namevalue = assignment.split("[:=]", 2);
//            String name = namevalue[0];
//            String value = namevalue[1];
//            values.put(name, value);
//        }
//
//        SocketOptions options = new SocketOptions();
//        Optional.ofNullable(values.get("read_timeout_ms")).map(Integer::parseInt).ifPresent(
//                options::setReadTimeoutMillis
//        );
//        Optional.ofNullable(values.get("connect_timeout_ms")).map(Integer::parseInt).ifPresent(
//                options::setConnectTimeoutMillis
//        );
//        Optional.ofNullable(values.get("keep_alive")).map(Boolean::parseBoolean).ifPresent(
//                options::setKeepAlive
//        );
//        Optional.ofNullable(values.get("reuse_address")).map(Boolean::parseBoolean).ifPresent(
//                options::setReuseAddress
//        );
//        Optional.ofNullable(values.get("so_linger")).map(Integer::parseInt).ifPresent(
//                options::setSoLinger
//        );
//        Optional.ofNullable(values.get("tcp_no_delay")).map(Boolean::parseBoolean).ifPresent(
//                options::setTcpNoDelay
//        );
//        Optional.ofNullable(values.get("receive_buffer_size")).map(Integer::parseInt).ifPresent(
//                options::setReceiveBufferSize
//        );
//        Optional.ofNullable(values.get("send_buffer_size")).map(Integer::parseInt).ifPresent(
//                options::setSendBufferSize
//        );
//
//        return options;
//    }
//
//    public static SpeculativeExecutionPolicy defaultSpeculativePolicy() {
//        PerHostPercentileTracker tracker = PerHostPercentileTracker
//                .builder(15000)
//                .build();
//        PercentileSpeculativeExecutionPolicy defaultSpecPolicy =
//                new PercentileSpeculativeExecutionPolicy(tracker, 99.0, 5);
//        return defaultSpecPolicy;
//    }
//
//    public static SpeculativeExecutionPolicy speculativeFor(String spec) {
//        Matcher pctileMatcher = PERCENTILE_EAGER_PATTERN.matcher(spec);
//        Matcher constantMatcher = CONSTANT_EAGER_PATTERN.matcher(spec);
//        if (pctileMatcher.matches()) {
//            double pctile = Double.valueOf(pctileMatcher.group("pctile"));
//            if (pctile > 100.0 || pctile < 0.0) {
//                throw new RuntimeException("pctile must be between 0.0 and 100.0");
//            }
//            String executionsSpec = pctileMatcher.group("executions");
//            String trackedSpec = pctileMatcher.group("tracked");
//            int executions = (executionsSpec != null && !executionsSpec.isEmpty()) ? Integer.valueOf(executionsSpec) : 5;
//            int tracked = (trackedSpec != null && !trackedSpec.isEmpty()) ? Integer.valueOf(trackedSpec) : 15000;
//            logger.debug("speculative: Creating new percentile tracker policy from spec '" + spec + "'");
//            return percentilePolicy(tracked, pctile, executions);
//        } else if (constantMatcher.matches()) {
//            int threshold = Integer.valueOf(constantMatcher.group("msThreshold"));
//            String executionsSpec = constantMatcher.group("executions");
//            int executions = (executionsSpec != null && !executionsSpec.isEmpty()) ? Integer.valueOf(executionsSpec) : 5;
//            logger.debug("speculative: Creating new constant policy from spec '" + spec + "'");
//            return constantPolicy(threshold, executions);
//        } else {
//            throw new RuntimeException("Unable to parse pattern for speculative option: " + spec + ", it must be in " +
//                    "an accepted form, like p99.0:5:15000, or p99.0:5, or 5000ms:5");
//        }
//
//    }
//
//    public static LoadBalancingPolicy whitelistFor(String s, LoadBalancingPolicy innerPolicy) {
//        String[] addrSpecs = s.split(",");
//        List<InetSocketAddress> sockAddrs = Arrays.stream(addrSpecs)
//                .map(CQLOptions::toSocketAddr)
//                .collect(Collectors.toList());
//        if (innerPolicy == null) {
//            innerPolicy = new RoundRobinPolicy();
//        }
//        return new WhiteListPolicy(innerPolicy, sockAddrs);
//    }
//
//    public static NettyOptions withTickDuration(String tick) {
//        logger.info("Cluster builder using custom tick duration value for HashedWheelTimer: " + tick + " milliseconds");
//        int tickDuration = Integer.valueOf(tick);
//        return new NettyOptions() {
//            public io.netty.util.Timer timer(ThreadFactory threadFactory) {
//                return new HashedWheelTimer(
//                        threadFactory, tickDuration, TimeUnit.MILLISECONDS);
//            }
//        };
//    }
//
//    private static InetSocketAddress toSocketAddr(String addr) {
//        String[] addrs = addr.split(":", 2);
//        String inetHost = addrs[0];
//        String inetPort = (addrs.length == 2) ? addrs[1] : "9042";
//        return new InetSocketAddress(inetHost, Integer.valueOf(inetPort));
//    }
//
//    public static ProtocolOptions.Compression withCompression(String compspec) {
//        try {
//            return ProtocolOptions.Compression.valueOf(compspec);
//        } catch (IllegalArgumentException iae) {
//            throw new RuntimeException("Compression option '" + compspec + "' was specified, but only " +
//                    Arrays.toString(ProtocolOptions.Compression.values()) + " are available.");
//        }
//    }
}
