package io.nosqlbench.activitytype.cqld4.statements.core;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.*;
import com.datastax.oss.driver.api.core.loadbalancing.LoadBalancingPolicy;
import com.datastax.oss.driver.api.core.metadata.EndPoint;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.retry.RetryPolicy;
import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.api.core.specex.SpeculativeExecutionPolicy;
import com.datastax.oss.driver.internal.core.config.map.MapBasedDriverConfigLoader;
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader;
import com.datastax.oss.driver.internal.core.retry.DefaultRetryPolicy;
import com.typesafe.config.ConfigFactory;
import io.nosqlbench.activitytype.cqld4.config.CQLD4OptionsMapper;
import io.nosqlbench.activitytype.cqld4.core.CQLOptions;
import io.nosqlbench.activitytype.cqld4.core.ProxyTranslator;
import io.nosqlbench.engine.api.activityapi.core.Shutdownable;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.api.scripting.NashornEvaluator;
import io.nosqlbench.engine.api.util.SSLKsFactory;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.graalvm.options.OptionMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CQLSessionCache implements Shutdownable {

    private final static Logger logger = LogManager.getLogger(CQLSessionCache.class);
    private final static String DEFAULT_SESSION_ID = "default";
    private static final CQLSessionCache instance = new CQLSessionCache();
    private final Map<String, SessionConfig> sessionCache = new HashMap<>();


    public final static class SessionConfig extends ConcurrentHashMap<String,String> {
        public CqlSession session;
        public OptionsMap optionsMap;

        public SessionConfig(CqlSession session, OptionsMap optionsMap) {
            this.session = session;
            this.optionsMap = optionsMap;
        }

        public void set(DefaultDriverOption intOption, Object value) {
            CQLD4OptionsMapper.apply(optionsMap,intOption.getPath(), value.toString());
        }
    }

    private CQLSessionCache() {
    }

    public static CQLSessionCache get() {
        return instance;
    }

    public void stopSession(ActivityDef activityDef) {
        String key = activityDef.getParams().getOptionalString("sessionid").orElse(DEFAULT_SESSION_ID);
        SessionConfig sessionConfig = sessionCache.get(key);
        sessionConfig.session.close();
    }

    public SessionConfig getSession(ActivityDef activityDef) {
        String key = activityDef.getParams().getOptionalString("sessionid").orElse(DEFAULT_SESSION_ID);
        String profileName = activityDef.getParams().getOptionalString("profile").orElse("default");
        SessionConfig sessionConfig = sessionCache.computeIfAbsent(key, (cid) -> createSession(activityDef, key, profileName));
        return sessionConfig;
    }

    // cbopts=\".withLoadBalancingPolicy(LatencyAwarePolicy.builder(new TokenAwarePolicy(new DCAwareRoundRobinPolicy(\"dc1-us-east\", 0, false))).build()).withRetryPolicy(new LoggingRetryPolicy(DefaultRetryPolicy.INSTANCE))\"

    private SessionConfig createSession(ActivityDef activityDef, String sessid, String profileName) {

        String host = activityDef.getParams().getOptionalString("host").orElse("localhost");
        int port = activityDef.getParams().getOptionalInteger("port").orElse(9042);

        activityDef.getParams().getOptionalString("cqldriver").ifPresent(v -> {
            logger.warn("The cqldriver parameter is not needed in this version of the driver.");
        });


        // TODO: Figure out how to layer configs with the new TypeSafe Config layer in the Datastax Java Driver
        // TODO: Or give up and bulk import options into the map, because the config API is a labyrinth

        CqlSessionBuilder builder = CqlSession.builder();
//
//        OptionsMap optionsMap = new OptionsMap();
//
//        OptionsMap defaults = OptionsMap.driverDefaults();
//        DriverConfigLoader cl = DriverConfigLoader.fromMap(defaults);
//        DriverConfig cfg = cl.getInitialConfig();


        Optional<Path> scb = activityDef.getParams().getOptionalString("secureconnectbundle")
            .map(Path::of);

        Optional<List<String>> hosts = activityDef.getParams().getOptionalString("host", "hosts")
            .map(h -> h.split(",")).map(Arrays::asList);

        Optional<Integer> port1 = activityDef.getParams().getOptionalInteger("port");


        if (scb.isPresent()) {
            scb.map(b -> {
                logger.debug("adding secureconnectbundle: " + b.toString());
                return b;
            }).ifPresent(builder::withCloudSecureConnectBundle);

            if (hosts.isPresent()) {
                logger.warn("The host parameter is not valid when using secureconnectbundle=");
            }
            if (port1.isPresent()) {
                logger.warn("the port parameter is not used with CQL when using secureconnectbundle=");
            }
        } else {
            hosts.orElse(List.of("localhost"))
                .stream()
                .map(h -> InetSocketAddress.createUnresolved(h,port))
                .peek(h-> logger.debug("adding contact endpoint: " + h.getHostName()+":"+h.getPort()))
                .forEachOrdered(builder::addContactPoint);
        }

//        builder.withCompression(ProtocolOptions.Compression.NONE);
        // TODO add map based configuration with compression defaults

        Optional<String> usernameOpt = activityDef.getParams().getOptionalString("username");
        Optional<String> passwordOpt = activityDef.getParams().getOptionalString("password");
        Optional<String> passfileOpt = activityDef.getParams().getOptionalString("passfile");
        Optional<String> authIdOpt = activityDef.getParams().getOptionalString("authid");


        if (usernameOpt.isPresent()) {
            String username = usernameOpt.get();
            String password;
            if (passwordOpt.isPresent()) {
                password = passwordOpt.get();
            } else if (passfileOpt.isPresent()) {
                Path path = Paths.get(passfileOpt.get());
                try {
                    password = Files.readAllLines(path).get(0);
                } catch (IOException e) {
                    String error = "Error while reading password from file:" + passfileOpt;
                    logger.error(error, e);
                    throw new RuntimeException(e);
                }
            } else {
                String error = "username is present, but neither password nor passfile are defined.";
                logger.error(error);
                throw new RuntimeException(error);
            }
            if (authIdOpt.isPresent()) {
                builder.withAuthCredentials(username, password, authIdOpt.get());
            } else {
                builder.withAuthCredentials(username, password);
            }
        }


        Optional<String> clusteropts = activityDef.getParams().getOptionalString("cbopts");
        if (clusteropts.isPresent()) {
            try {
                logger.info("applying cbopts:" + clusteropts.get());
                NashornEvaluator<CqlSessionBuilder> clusterEval = new NashornEvaluator<>(CqlSessionBuilder.class);
                clusterEval.put("builder", builder);
                String importEnv =
                        "load(\"nashorn:mozilla_compat.js\");\n" +
                                " importPackage(com.google.common.collect.Lists);\n" +
                                " importPackage(com.google.common.collect.Maps);\n" +
                                " importPackage(com.datastax.driver);\n" +
                                " importPackage(com.datastax.driver.core);\n" +
                                " importPackage(com.datastax.driver.core.policies);\n" +
                                "builder" + clusteropts.get() + "\n";
                clusterEval.script(importEnv);
                builder = clusterEval.eval();
                logger.info("successfully applied:" + clusteropts.get());
            } catch (Exception e) {
                logger.error("Unable to evaluate: " + clusteropts.get() + " in script context:" + e.getMessage());
                throw e;
            }
        }

        // TODO: Support speculative=>
//        SpeculativeExecutionPolicy speculativePolicy = activityDef.getParams()
//                .getOptionalString("speculative")
//                .map(speculative -> {
//                    logger.info("speculative=>" + speculative);
//                    return speculative;
//                })
//                .map(CQLOptions::speculativeFor)
//                .orElse(CQLOptions.defaultSpeculativePolicy());
//        builder.withSpeculativeExecutionPolicy(speculativePolicy);

        // TODO: Support socketoptions=>
//        activityDef.getParams().getOptionalString("socketoptions")
//                .map(sockopts -> {
//                    logger.info("socketoptions=>" + sockopts);
//                    return sockopts;
//                })
//                .map(CQLOptions::socketOptionsFor)
//                .ifPresent(builder::withSocketOptions);
//

        // TODO: Support reconnectpolicy
//        activityDef.getParams().getOptionalString("reconnectpolicy")
//            .map(reconnectpolicy-> {
//                logger.info("reconnectpolicy=>" + reconnectpolicy);
//                return reconnectpolicy;
//            })
//            .map(CQLOptions::reconnectPolicyFor)
//            .ifPresent(builder::withReconnectionPolicy);


        // TODO: support pooling options
//        activityDef.getParams().getOptionalString("pooling")
//                .map(pooling -> {
//                    logger.info("pooling=>" + pooling);
//                    return pooling;
//                })
//                .map(CQLOptions::poolingOptionsFor)
//                .ifPresent(builder::withPoolingOptions);

        // TODO: support whitelist options
//        activityDef.getParams().getOptionalString("whitelist")
//                .map(whitelist -> {
//                    logger.info("whitelist=>" + whitelist);
//                    return whitelist;
//                })
//                .map(p -> CQLOptions.whitelistFor(p, null))
//                .ifPresent(builder::withLoadBalancingPolicy);
//

        // TODO: support tickduration
//        activityDef.getParams().getOptionalString("tickduration")
//                .map(tickduration -> {
//                    logger.info("tickduration=>" + tickduration);
//                    return tickduration;
//                })
//                .map(CQLOptions::withTickDuration)
//                .ifPresent(builder::withNettyOptions);

        // TODO: support compression
//        activityDef.getParams().getOptionalString("compression")
//                .map(compression -> {
//                    logger.info("compression=>" + compression);
//                    return compression;
//                })
//                .map(CQLOptions::withCompression)
//                .ifPresent(builder::withCompression);

        // TODO: Support SSL standard config interface
//        if (activityDef.getParams().getOptionalString("ssl").isPresent()) {
//            logger.info("Cluster builder proceeding with SSL but no Client Auth");
//            Object context = SSLKsFactory.get().getContext(activityDef);
//            SSLOptions sslOptions;
//            if (context instanceof javax.net.ssl.SSLContext) {
//                sslOptions = RemoteEndpointAwareJdkSSLOptions.builder()
//                        .withSSLContext((javax.net.ssl.SSLContext) context).build();
//                builder.withSSL(sslOptions);
//            } else if (context instanceof io.netty.handler.ssl.SslContext) {
//                sslOptions =
//                        new RemoteEndpointAwareNettySSLOptions((io.netty.handler.ssl.SslContext) context);
//            } else {
//                throw new RuntimeException("Unrecognized ssl context object type: " + context.getClass().getCanonicalName());
//            }
//            builder.withSSL(sslOptions);
//        }

        // TODO: Support retry policy
//        RetryPolicy retryPolicy = activityDef.getParams()
//                .getOptionalString("retrypolicy")
//                .map(CQLOptions::retryPolicyFor).orElse(DefaultRetryPolicy.INSTANCE);
//
//        if (retryPolicy instanceof LoggingRetryPolicy) {
//            logger.info("using LoggingRetryPolicy");
//        }
//
//        builder.withRetryPolicy(retryPolicy);

        // TODO: Support JMX reporting toggle
//        if (!activityDef.getParams().getOptionalBoolean("jmxreporting").orElse(false)) {
//            builder.withoutJMXReporting();
//        }

        // TODO: Support single-endpoint options?
//        // Proxy Translator and Whitelist for use with DS Cloud on-demand single-endpoint setup
//        if (activityDef.getParams().getOptionalBoolean("single-endpoint").orElse(false)) {
//            InetSocketAddress inetHost = new InetSocketAddress(host, port);
//            final List<InetSocketAddress> whiteList = new ArrayList<>();
//            whiteList.add(inetHost);
//
//            LoadBalancingPolicy whitelistPolicy = new WhiteListPolicy(new RoundRobinPolicy(), whiteList);
//            builder.withAddressTranslator(new ProxyTranslator(inetHost)).withLoadBalancingPolicy(whitelistPolicy);
//        }

        CqlSession session = builder.build();

//        Cluster cl = builder.build();


        // TODO: Support default idempotence
//        // Apply default idempotence, if set
//        activityDef.getParams().getOptionalBoolean("defaultidempotence").map(
//                b -> cl.getConfiguration().getQueryOptions().setDefaultIdempotence(b)
//        );

        // This also forces init of metadata


        Map<UUID, Node> nodes = session.getMetadata().getNodes();
        if (nodes.size()>25) {
            logger.info("Found " + nodes.size() + " nodes in cluster.");
        } else {
            nodes.forEach((k,v)->{
                logger.info("found node " + k);
            });
        }
        logger.info("cluster-metadata-allhosts:\n" + session.getMetadata().getNodes());

        if (activityDef.getParams().getOptionalBoolean("drivermetrics").orElse(false)) {
            String driverPrefix = activityDef.getParams().getOptionalString("driverprefix").orElse("driver."+sessid) + ".";
            session.getMetrics().ifPresent(m -> ActivityMetrics.mountSubRegistry(driverPrefix,m.getRegistry()));
        }

        OptionsMap optionsMap = OptionsMap.driverDefaults();

        return new SessionConfig(session,optionsMap);
    }

    @Override
    public void shutdown() {
        for (SessionConfig session : sessionCache.values()) {
            session.session.close();
        }
    }
}
