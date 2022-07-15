/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapter.cqld4.optionhelpers;

import com.datastax.oss.driver.api.core.DefaultProtocolVersion;
import com.datastax.oss.driver.api.core.ProtocolVersion;
import com.datastax.oss.driver.api.core.config.OptionsMap;
import com.datastax.oss.driver.api.core.config.TypedDriverOption;
import com.datastax.oss.driver.internal.core.connection.ExponentialReconnectionPolicy;
import com.datastax.oss.driver.internal.core.specex.ConstantSpeculativeExecutionPolicy;
import io.nosqlbench.api.config.standard.*;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provide a concise way to express configuration option helpers which simplify
 * usage of the long-form options with the latest driver. Essentially, make
 * lambda-style definition of optional parameter handling _readable_, and
 * provide a working blueprint for how to handle config helpers.
 */
public class OptionHelpers implements NBConfigurable {

    private final static Logger logger = LogManager.getLogger(OptionHelpers.class);

    private final Map<String, Modifier> modifiers = new LinkedHashMap<>();
    private final Map<String, String> descriptions = new LinkedHashMap<>();
    private final OptionsMap options;

    public OptionHelpers(OptionsMap options) {
        this.options = options;
        addModifiers();
    }

    private void addModifiers() {
        add("insights", "Insights Reporting", (m, v) -> {
            m.put(TypedDriverOption.MONITOR_REPORTING_ENABLED, Boolean.parseBoolean(v));
        });

        Pattern CONSTANT_EAGER_PATTERN = Pattern.compile("^((?<msThreshold>\\d++)ms)(:(?<executions>\\d+))?$");
        Pattern PERCENTILE_EAGER_PATTERN = Pattern.compile("^p(?<pctile>[^:]+)(:(?<executions>\\d+))?(:(?<tracked>\\d+)ms)?$");

        add("speculative", "Speculative Execution", (m, v) -> {
            if (PERCENTILE_EAGER_PATTERN.matcher(v).matches()) {
                throw new RuntimeException("Option 'speculative' with percentile thresholds (" + v + ") is not supported in driver 4." +
                    " If you want to provide a custom speculative execution policy, you can configure it directly via the Java driver options.");
            }
            Matcher constantMatcher = CONSTANT_EAGER_PATTERN.matcher(v);
            if (constantMatcher.matches()) {
                int threshold = Integer.valueOf(constantMatcher.group("msThreshold"));
                String executionsSpec = constantMatcher.group("executions");
                int executions = (executionsSpec != null && !executionsSpec.isEmpty()) ? Integer.parseInt(executionsSpec) : 5;

                m.put(TypedDriverOption.SPECULATIVE_EXECUTION_MAX, threshold);
                m.put(TypedDriverOption.SPECULATIVE_EXECUTION_DELAY, Duration.ofMillis(executions));
                m.put(TypedDriverOption.SPECULATIVE_EXECUTION_POLICY_CLASS, ConstantSpeculativeExecutionPolicy.class.getSimpleName());
            }
        });

        add("protocol_version", "Protocol Version", (m, v) -> {
            String version = v.toUpperCase(Locale.ROOT);
            try {
                DefaultProtocolVersion defaultProtocolVersion = DefaultProtocolVersion.valueOf(version);
                version = defaultProtocolVersion.toString();
            } catch (IllegalArgumentException iae) {
                try {
                    Field field = ProtocolVersion.class.getField(version);
                } catch (NoSuchFieldException e) {
                    Set<String> known = new HashSet<>();
                    for (DefaultProtocolVersion value : DefaultProtocolVersion.values()) {
                        known.add(value.toString());
                    }
                    for (Field field : ProtocolVersion.class.getFields()) {
                        known.add(field.getName());
                    }
                    throw new RuntimeException("There was no protocol name that matched '" + v + "'. The known values are " + known.stream().sorted().toList().toString());
                }
            }
            m.put(TypedDriverOption.PROTOCOL_VERSION, version);
        });

        add("socket_options", "Socket Options", (m, v) -> {
            String[] assignments = v.split("[,;]");
            Map<String, String> values = new HashMap<>();
            for (String assignment : assignments) {
                String[] namevalue = assignment.split("[:=]", 2);
                String name = namevalue[0];
                String value = namevalue[1];
                values.put(name, value);
            }

            Optional.ofNullable(values.remove("read_timeout_ms")).map(Integer::parseInt)
                .ifPresent(i -> {
                    logger.warn("Since the driver parameters do not map directly from previous versions to driver 4," +
                        " the 'read_timeout_ms' option is being applied to all configurable timeout parameters. If you want" +
                        " to customize this, do not set read_timeout_ms directly, but instead set the individual timeouts" +
                        " as documented for CQL Java driver 4.*");
                    m.put(TypedDriverOption.GRAPH_TIMEOUT, Duration.ofMillis(i));
                    m.put(TypedDriverOption.REQUEST_TIMEOUT, Duration.ofMillis(i));
                    m.put(TypedDriverOption.HEARTBEAT_TIMEOUT, Duration.ofMillis(i));
                    m.put(TypedDriverOption.REPREPARE_TIMEOUT, Duration.ofMillis(i));
                    m.put(TypedDriverOption.CONTROL_CONNECTION_TIMEOUT, Duration.ofMillis(i));
                    m.put(TypedDriverOption.CONNECTION_CONNECT_TIMEOUT, Duration.ofMillis(i));
                    m.put(TypedDriverOption.NETTY_ADMIN_SHUTDOWN_TIMEOUT, i);
                    m.put(TypedDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofMillis(i));
                    m.put(TypedDriverOption.CONNECTION_SET_KEYSPACE_TIMEOUT, Duration.ofMillis(i));
                    m.put(TypedDriverOption.METADATA_SCHEMA_REQUEST_TIMEOUT, Duration.ofMillis(i));
                    m.put(TypedDriverOption.METADATA_SCHEMA_REQUEST_TIMEOUT, Duration.ofMillis(i));
                    m.put(TypedDriverOption.CONTROL_CONNECTION_AGREEMENT_TIMEOUT, Duration.ofMillis(i));
                    m.put(TypedDriverOption.CONTINUOUS_PAGING_TIMEOUT_FIRST_PAGE, Duration.ofMillis(i));
                    m.put(TypedDriverOption.CONTINUOUS_PAGING_TIMEOUT_OTHER_PAGES, Duration.ofMillis(i));
                });

            Optional.ofNullable(values.remove("connect_timeout_ms")).map(Integer::parseInt)
                .ifPresent(i -> m.put(TypedDriverOption.CONNECTION_CONNECT_TIMEOUT, Duration.ofMillis(i)));

            Optional.ofNullable(values.remove("keep_alive")).map(Boolean::parseBoolean)
                .ifPresent(ka -> m.put(TypedDriverOption.SOCKET_KEEP_ALIVE, ka));

            Optional.ofNullable(values.remove("reuse_address")).map(Boolean::parseBoolean)
                .ifPresent(ru -> m.put(TypedDriverOption.SOCKET_REUSE_ADDRESS, ru));

            Optional.ofNullable(values.remove("so_linger")).map(Integer::parseInt)
                .ifPresent(li -> m.put(TypedDriverOption.SOCKET_LINGER_INTERVAL, li));

            Optional.ofNullable(values.remove("tcp_no_delay")).map(Boolean::parseBoolean)
                .ifPresent(nd -> m.put(TypedDriverOption.SOCKET_TCP_NODELAY, nd));

            Optional.ofNullable(values.remove("receive_buffer_size")).map(Integer::parseInt)
                .ifPresent(bs -> m.put(TypedDriverOption.SOCKET_RECEIVE_BUFFER_SIZE, bs));

            Optional.ofNullable(values.remove("send_buffer_size")).map(Integer::parseInt)
                .ifPresent(bs -> m.put(TypedDriverOption.SOCKET_SEND_BUFFER_SIZE, bs));

            for (String s : values.keySet()) {
                throw new RuntimeException("socket_options field '" + s + "' was not recognized.");
            }

        });

        add("reconnect_policy", "Reconnect Policy", (m, spec) -> {
            if (spec.startsWith("exponential(")) {
                String argsString = spec.substring(12);
                String[] args = argsString.substring(0, argsString.length() - 1).split("[,;]");
                if (args.length != 2) {
                    throw new BasicError("Invalid reconnect_policy, try reconnect_policy=exponential(<baseDelay>, <maxDelay>)");
                }
                long baseDelay = Long.parseLong(args[0]);
                long maxDelay = Long.parseLong(args[1]);
                m.put(TypedDriverOption.RECONNECTION_POLICY_CLASS, ExponentialReconnectionPolicy.class.getSimpleName());
                m.put(TypedDriverOption.RECONNECTION_BASE_DELAY, Duration.ofMillis(baseDelay));
                m.put(TypedDriverOption.RECONNECTION_MAX_DELAY, Duration.ofMillis(maxDelay));
            }
        });

        add("pooling", "Pooling Options", (m, spec) -> {
            Pattern CORE_AND_MAX_RQ_PATTERN = Pattern.compile(
                "(?<core>\\d+)(:(?<max>\\d+)(:(?<rq>\\d+))?)?(,(?<rcore>\\d+)(:(?<rmax>\\d+)(:(?<rrq>\\d+))?)?)?(,?heartbeat_interval_s:(?<heartbeatinterval>\\d+))?(,?heartbeat_timeout_s:(?<heartbeattimeout>\\d+))?(,?idle_timeout_s:(?<idletimeout>\\d+))?(,?pool_timeout_ms:(?<pooltimeout>\\d+))?"
            );

            Matcher matcher = CORE_AND_MAX_RQ_PATTERN.matcher(spec);
            if (matcher.matches()) {

                Optional<Integer> coreLocal = Optional.ofNullable(matcher.group("core")).map(Integer::valueOf);
                Optional<Integer> maxLocal = Optional.ofNullable(matcher.group("max")).map(Integer::valueOf);
                Optional<Integer> localRq = Optional.ofNullable(matcher.group("rq")).map(Integer::valueOf);
                if (coreLocal.isPresent() && maxLocal.isPresent() && !coreLocal.get().equals(maxLocal.get())) {
                    throw new RuntimeException("In CQL Java driver 4, core and max connections have been reduced to a single value." +
                        " You have two different values in (" + spec + "). If you make them the same, you can continue to use the 'pooling' helper option.");
                }
                coreLocal.ifPresent(i -> m.put(TypedDriverOption.CONNECTION_POOL_LOCAL_SIZE, i));
                localRq.ifPresent(r -> m.put(TypedDriverOption.CONNECTION_MAX_REQUESTS, r));

                Optional<Integer> coreRemote = Optional.ofNullable(matcher.group("rcore")).map(Integer::valueOf);
                Optional<Integer> maxRemote = Optional.ofNullable(matcher.group("rmax")).map(Integer::valueOf);
                Optional<Integer> rqRemote = Optional.ofNullable(matcher.group("rrq")).map(Integer::valueOf);
                if (coreRemote.isPresent() && maxRemote.isPresent() && !coreRemote.get().equals(maxRemote.get())) {
                    throw new RuntimeException("In CQL Java driver 4, rcore and rmax connections have been reduced to a single value." +
                        " You have two different values in (" + spec + "). If you make them the same, you can continue to use the 'pooling' helper option." +
                        " Otherwise, set the driver options directly according to driver 4 docs.");
                }

                if (localRq.isPresent() && rqRemote.isPresent() && !localRq.get().equals(rqRemote.get())) {
                    throw new RuntimeException("In CQL Java driver 4, remote and local max requests per connection have been reduced to a single value." +
                        " You have two different values in (" + spec + "). If you make them the same, you can continue to use the 'pooling' helper option." +
                        " Otherwise, set the driver options directly according to driver 4 docs.");
                }
                coreRemote.ifPresent(i -> m.put(TypedDriverOption.CONNECTION_POOL_REMOTE_SIZE, i));
                localRq.ifPresent(r -> m.put(TypedDriverOption.CONNECTION_MAX_REQUESTS, r));

                Optional.ofNullable(matcher.group("heartbeatinterval")).map(Integer::valueOf)
                    .ifPresent(hbi -> m.put(TypedDriverOption.HEARTBEAT_INTERVAL, Duration.ofMillis(hbi)));

                Optional.ofNullable(matcher.group("heartbeattimeout")).map(Integer::valueOf)
                    .ifPresent(ito -> m.put(TypedDriverOption.HEARTBEAT_TIMEOUT, Duration.ofMillis(ito)));

                Optional.ofNullable(matcher.group("idletimeout")).map(Integer::valueOf)
                    .ifPresent(ito -> {
                        logger.warn(("Since this parameter doesn't have a direct equivalent in CQL driver 4, it is being applied to HEARTBEAT_TIMEOUT."));
                        m.put(TypedDriverOption.HEARTBEAT_TIMEOUT, Duration.ofMillis(ito));
                    });
            } else {
                throw new RuntimeException("No pooling options could be parsed from spec: " + spec);
            }
        });

        add("lbp","load balancer policy (deprecated)",(m,v) -> {
            throw new RuntimeException("Composable load balancers have been removed in Java driver 4 unless you provide a custom implementation.");
        });
        add("loadbalancingpolicy","load balancing policy (deprecated)",(m,v) -> {
            throw new RuntimeException("Composable load balancers have been removed in Java driver 4 unless you provide a custom implementation.");
        });

        add("tickduration","Netty Tick Duration",(m,v) -> {
            m.put(TypedDriverOption.NETTY_TIMER_TICK_DURATION,Duration.ofMillis(Long.parseLong(v)));
        });

        add("compression","Compression",(m,v) -> {
            m.put(TypedDriverOption.PROTOCOL_COMPRESSION,v);
        });

        add("retrypolicy","Retry Policy",(m,v) -> {
            m.put(TypedDriverOption.RETRY_POLICY_CLASS,v);
        });

        add("jmxreporting","",(m,v) -> {
            throw new RuntimeException("enabling or disabling JMX reporting is not supported in Java driver 4.*");
        });

        add("single-endpoint","",(m,v) -> {
            throw new RuntimeException("the proxy translator setting has been removed from CQL driver 4. You might be interested in setting cloud_proxy_address.");
        });

        add("haproxy_source_ip","",(m,v) -> {});

        add("defaultidempotence","",(m,v) -> {});

        add("drivermetrics","",(m,v) -> {});


    }

    public void add(String name, String description, Modifier modifier) {
        this.modifiers.put(name, modifier);
        this.descriptions.put(name,description);
    }

    @Override
    public void applyConfig(NBConfiguration cfg) {
        Map<String, Object> values = cfg.getMap();
        for (String paramName : values.keySet()) {
            if (modifiers.containsKey(paramName)) {
                modifiers.get(paramName).accept(options, values.get(paramName).toString());
            }
        }
    }

    @Override
    public NBConfigModel getConfigModel() {
        ConfigModel model = ConfigModel.of(OptionHelpers.class);
        modifiers.forEach((k, v) -> model.add(Param.optional(k, String.class)));
        return model.asReadOnly();
    }

    public interface Modifier extends BiConsumer<OptionsMap, String> { }
}
