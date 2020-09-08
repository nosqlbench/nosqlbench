package io.nosqlbench.activitytype.cql.statements.core;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.core.RemoteEndpointAwareJdkSSLOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.*;
import com.datastax.driver.dse.DseCluster;
import io.nosqlbench.activitytype.cql.core.CQLOptions;
import io.nosqlbench.activitytype.cql.core.ProxyTranslator;
import io.nosqlbench.engine.api.activityapi.core.Shutdownable;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.api.scripting.NashornEvaluator;
import io.nosqlbench.engine.api.util.SSLKsFactory;
import io.nosqlbench.nb.api.errors.BasicError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CQLSessionCache implements Shutdownable {

    private final static Logger logger = LoggerFactory.getLogger(CQLSessionCache.class);
    private final static String DEFAULT_SESSION_ID = "default";
    private static final CQLSessionCache instance = new CQLSessionCache();
    private final Map<String, Session> sessionCache = new HashMap<>();

    private CQLSessionCache() {
    }

    public static CQLSessionCache get() {
        return instance;
    }

    public void stopSession(ActivityDef activityDef) {
        String key = activityDef.getParams().getOptionalString("clusterid").orElse(DEFAULT_SESSION_ID);
        Session session = sessionCache.get(key);
        session.getCluster().close();
        session.close();
    }

    public Session getSession(ActivityDef activityDef) {
        String key = activityDef.getParams().getOptionalString("clusterid").orElse(DEFAULT_SESSION_ID);
        return sessionCache.computeIfAbsent(key, (cid) -> createSession(activityDef, key));
    }

    // cbopts=\".withLoadBalancingPolicy(LatencyAwarePolicy.builder(new TokenAwarePolicy(new DCAwareRoundRobinPolicy(\"dc1-us-east\", 0, false))).build()).withRetryPolicy(new LoggingRetryPolicy(DefaultRetryPolicy.INSTANCE))\"

    private Session createSession(ActivityDef activityDef, String sessid) {

        String host = activityDef.getParams().getOptionalString("host").orElse("localhost");
        int port = activityDef.getParams().getOptionalInteger("port").orElse(9042);

        String driverType = activityDef.getParams().getOptionalString("cqldriver").orElse("dse");

        Cluster.Builder builder =
            driverType.toLowerCase().equals("dse") ? DseCluster.builder() :
                driverType.toLowerCase().equals("oss") ? Cluster.builder() : null;

        if (builder == null) {
            throw new RuntimeException("The driver type '" + driverType + "' is not recognized");
        }

        logger.info("Using driver type '" + driverType.toUpperCase() + "'");

        Optional<String> scb = activityDef.getParams()
            .getOptionalString("secureconnectbundle");
        scb.map(File::new)
            .ifPresent(builder::withCloudSecureConnectBundle);

        activityDef.getParams()
            .getOptionalString("insights").map(Boolean::parseBoolean)
            .ifPresent(builder::withMonitorReporting);

        String[] contactPoints = activityDef.getParams().getOptionalString("host")
            .map(h -> h.split(",")).orElse(null);

        if (contactPoints == null) {
            contactPoints = activityDef.getParams().getOptionalString("hosts")
                .map(h -> h.split(",")).orElse(null);
        }
        if (contactPoints == null && scb.isEmpty()) {
            contactPoints = new String[]{"localhost"};
        }

        if (contactPoints != null) {
            builder.addContactPoints(contactPoints);
        }

        activityDef.getParams().getOptionalInteger("port").ifPresent(builder::withPort);

        builder.withCompression(ProtocolOptions.Compression.NONE);

        Optional<String> usernameOpt = activityDef.getParams().getOptionalString("username");
        Optional<String> passwordOpt = activityDef.getParams().getOptionalString("password");
        Optional<String> passfileOpt = activityDef.getParams().getOptionalString("passfile");

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
            builder.withCredentials(username, password);
        }

        Optional<String> clusteropts = activityDef.getParams().getOptionalString("cbopts");
        if (clusteropts.isPresent()) {
            try {
                logger.info("applying cbopts:" + clusteropts.get());
                NashornEvaluator<DseCluster.Builder> clusterEval = new NashornEvaluator<>(DseCluster.Builder.class);
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
                throw new RuntimeException("Unable to evaluate: " + clusteropts.get() + " in script context:", e);
            }
        }

        if (activityDef.getParams().getOptionalString("whitelist").isPresent() &&
            activityDef.getParams().getOptionalString("lbp", "loadbalancingpolicy").isPresent()) {
            throw new BasicError("You specified both whitelist=.. and lbp=..., if you need whitelist and other policies together," +
                " be sure to use the lbp option only with a whitelist policy included.");
        }

        Optional<String> specSpec = activityDef.getParams()
            .getOptionalString("speculative");

        if (specSpec.isPresent()) {
            specSpec
                .map(speculative -> {
                    logger.info("speculative=>" + speculative);
                    return speculative;
                })
                .map(CQLOptions::speculativeFor)
                .ifPresent(builder::withSpeculativeExecutionPolicy);
        } else {
            logger.warn(
                "If the cql speculative parameter is not provided, it uses a default speculative\n" +
                    "policy, as in speculative=default, rather than leaving speculative off.\n" +
                    "If you want to keep this behavior or silence this warning, add one of\n" +
                    "speculative=none OR speculative=default to your activity params.\n" +
                    "This release, the default is speculative=default if the parameter is not specified.\n" +
                    "After Oct/2020, the default will be speculative=none if the parameter is not specified.\n" +
                    "Note: speculative=default is the same as speculative=p99.0:5:15000\n" +
                    "which is considered aggressive for some systems. This warning will go away after Oct/2020.\n");
            builder.withSpeculativeExecutionPolicy(CQLOptions.defaultSpeculativePolicy());
        }

        activityDef.getParams().getOptionalString("socketoptions")
            .map(sockopts -> {
                logger.info("socketoptions=>" + sockopts);
                return sockopts;
            })
            .map(CQLOptions::socketOptionsFor)
            .ifPresent(builder::withSocketOptions);

        activityDef.getParams().getOptionalString("reconnectpolicy")
            .map(reconnectpolicy -> {
                logger.info("reconnectpolicy=>" + reconnectpolicy);
                return reconnectpolicy;
            })
            .map(CQLOptions::reconnectPolicyFor)
            .ifPresent(builder::withReconnectionPolicy);


        activityDef.getParams().getOptionalString("pooling")
            .map(pooling -> {
                logger.info("pooling=>" + pooling);
                return pooling;
            })
            .map(CQLOptions::poolingOptionsFor)
            .ifPresent(builder::withPoolingOptions);

        activityDef.getParams().getOptionalString("whitelist")
            .map(whitelist -> {
                logger.info("whitelist=>" + whitelist);
                return whitelist;
            })
            .map(p -> CQLOptions.whitelistFor(p, null))
            .ifPresent(builder::withLoadBalancingPolicy);

        activityDef.getParams().getOptionalString("lbp")
            .map(lbp -> {
                logger.info("lbp=>" + lbp);
                return lbp;
            })
            .map(p -> CQLOptions.lbpolicyFor(p, null))
            .ifPresent(builder::withLoadBalancingPolicy);

        activityDef.getParams().getOptionalString("tickduration")
            .map(tickduration -> {
                logger.info("tickduration=>" + tickduration);
                return tickduration;
            })
            .map(CQLOptions::withTickDuration)
            .ifPresent(builder::withNettyOptions);

        activityDef.getParams().getOptionalString("compression")
            .map(compression -> {
                logger.info("compression=>" + compression);
                return compression;
            })
            .map(CQLOptions::withCompression)
            .ifPresent(builder::withCompression);


        SSLContext context = SSLKsFactory.get().getContext(activityDef);
        if (context != null) {
            builder.withSSL(RemoteEndpointAwareJdkSSLOptions.builder().withSSLContext(context).build());
        }

        RetryPolicy retryPolicy = activityDef.getParams()
            .getOptionalString("retrypolicy")
            .map(CQLOptions::retryPolicyFor).orElse(DefaultRetryPolicy.INSTANCE);

        if (retryPolicy instanceof LoggingRetryPolicy) {
            logger.info("using LoggingRetryPolicy");
        }

        builder.withRetryPolicy(retryPolicy);

        if (!activityDef.getParams().getOptionalBoolean("jmxreporting").orElse(false)) {
            builder.withoutJMXReporting();
        }

        // Proxy Translator and Whitelist for use with DS Cloud on-demand single-endpoint setup
        if (activityDef.getParams().getOptionalBoolean("single-endpoint").orElse(false)) {
            InetSocketAddress inetHost = new InetSocketAddress(host, port);
            final List<InetSocketAddress> whiteList = new ArrayList<>();
            whiteList.add(inetHost);

            LoadBalancingPolicy whitelistPolicy = new WhiteListPolicy(new RoundRobinPolicy(), whiteList);
            builder.withAddressTranslator(new ProxyTranslator(inetHost)).withLoadBalancingPolicy(whitelistPolicy);
        }

        Cluster cl = builder.build();

        // Apply default idempotence, if set
        activityDef.getParams().getOptionalBoolean("defaultidempotence").map(
            b -> cl.getConfiguration().getQueryOptions().setDefaultIdempotence(b)
        );

        Session session = cl.newSession();

        // This also forces init of metadata

        logger.info("cluster-metadata-allhosts:\n" + session.getCluster().getMetadata().getAllHosts());

        if (activityDef.getParams().getOptionalBoolean("drivermetrics").orElse(false)) {
            String driverPrefix = "driver." + sessid;
            driverPrefix = activityDef.getParams().getOptionalString("driverprefix").orElse(driverPrefix) + ".";
            ActivityMetrics.mountSubRegistry(driverPrefix, cl.getMetrics().getRegistry());
        }

        return session;
    }

    @Override
    public void shutdown() {
        for (Session session : sessionCache.values()) {
            Cluster cluster = session.getCluster();
            session.close();
            cluster.close();
        }
    }
}
