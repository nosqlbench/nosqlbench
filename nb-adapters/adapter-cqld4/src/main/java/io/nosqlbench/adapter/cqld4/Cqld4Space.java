/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.adapter.cqld4;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.*;
import com.datastax.oss.driver.api.core.session.SessionBuilder;
import com.datastax.oss.driver.internal.core.config.composite.CompositeDriverConfigLoader;
import com.datastax.oss.driver.internal.core.loadbalancing.helper.NodeFilterToDistanceEvaluatorAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.adapter.cqld4.optionhelpers.OptionHelpers;
import io.nosqlbench.adapter.cqld4.wrapper.Cqld4LoadBalancerObserver;
import io.nosqlbench.adapter.cqld4.wrapper.Cqld4SessionBuilder;
import io.nosqlbench.adapter.cqld4.wrapper.NodeSummary;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.nb.api.engine.util.SSLKsFactory;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Cqld4Space implements AutoCloseable {
    private final static Logger logger = LogManager.getLogger(Cqld4Space.class);
    private final String space;

    CqlSession session;

    public Cqld4Space(String space, NBConfiguration cfg) {
        this.space = space;
        session = createSession(cfg);
    }

    private static NBConfigModel getDriverOptionsModel() {
        ConfigModel driverOpts = ConfigModel.of(DriverConfig.class);
        Iterable<TypedDriverOption<?>> builtins = TypedDriverOption.builtInValues();
        for (TypedDriverOption<?> builtin : builtins) {
            String path = builtin.getRawOption().getPath();
            Class<?> rawType = builtin.getExpectedType().getRawType();
            driverOpts.add(Param.optional("driver." + path, rawType));
        }
        return driverOpts.asReadOnly();
    }

    private CqlSession createSession(NBConfiguration cfg) {

        NodeSummary diag = NodeSummary.valueOf(cfg.get("diag"));

        CqlSessionBuilder builder = switch (diag) {
            default -> new CqlSessionBuilder();
            case NodeSummary.addr, NodeSummary.mid, NodeSummary.all -> new Cqld4SessionBuilder();
        };

        // stop insights for testing
        OptionsMap defaults = new OptionsMap();
        defaults.put(TypedDriverOption.MONITOR_REPORTING_ENABLED, false); // We don't need to do this every time we run a test or sanity check
        DriverConfigLoader dcl = DriverConfigLoader.fromMap(defaults);

        // add streamlined cql parameters
        OptionHelpers helpers = new OptionHelpers(defaults);
        NBConfiguration cqlHelperCfg = helpers.getConfigModel().extractConfig(cfg);
        helpers.applyConfig(cqlHelperCfg);

        // add user-provided parameters
        NBConfiguration nbActivityDriverOptions = getDriverOptionsModel().extractConfig(cfg);
        if (!nbActivityDriverOptions.isEmpty()) {
            Map<String, Object> remapped = new LinkedHashMap<>();
            nbActivityDriverOptions.getMap().forEach((k, v) -> remapped.put(k.substring("driver.".length()), v));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String remappedViaSerdesToSatisfyObtuseConfigAPI = gson.toJson(remapped);
            DriverConfigLoader userProvidedOptions = DriverConfigLoader.fromString(remappedViaSerdesToSatisfyObtuseConfigAPI);
            dcl = new CompositeDriverConfigLoader(dcl, userProvidedOptions);
        }

        // add referenced config from 'cfg' activity parameter
        DriverConfigLoader cfgDefaults = resolveConfigLoader(cfg).orElse(DriverConfigLoader.fromMap(OptionsMap.driverDefaults()));
        dcl = new CompositeDriverConfigLoader(dcl, cfgDefaults);

//        int port = cfg.getOptional(int.class, "port").orElse(9042);

        Optional<String> scb = cfg.getOptional(String.class, "secureconnectbundle", "scb");

        if (scb.isPresent()) {
            Optional<InputStream> stream =
                scb.flatMap(s -> NBIO.all().pathname(s).first().map(Content::getInputStream));
            if (stream.isPresent()) {
                stream.map(builder::withCloudSecureConnectBundle);
            } else {
                String error = String.format("Unable to load Secure Connect Bundle from path %s", scb.get());
                logger.error(error);
                throw new RuntimeException(error);
            }
        }

        Optional<List<InetSocketAddress>> contactPointsOption = cfg
            .getOptional("host", "hosts")
            .map(s -> Arrays.asList(s.split(",")))
            .map(
                sl -> sl.stream()
                    .map(n -> new InetSocketAddress(n, cfg.getOptional(int.class, "port").orElse(9042)))
                    .collect(Collectors.toList())
            );

        if (contactPointsOption.isPresent()) {
            builder.addContactPoints(contactPointsOption.get());
            Optional<String> localdc = cfg.getOptional("localdc");
            builder.withLocalDatacenter(localdc.orElseThrow(
                () -> new BasicError("Starting with driver 4.0, you must specify the local datacenter name with any specified contact points. Example: (use caution) localdc=datacenter1")
            ));
        } else {
            builder.addContactPoints(List.of(new InetSocketAddress("localhost", cfg.getOptional(int.class, "port").orElse(9042))));
        }

//        builder.withCompression(ProtocolOptions.Compression.NONE);
//
        Optional<String> usernameOpt = cfg.getOptional("username");
        Optional<String> userfileOpt = cfg.getOptional("userfile");
        Optional<String> passwordOpt = cfg.getOptional("password");
        Optional<String> passfileOpt = cfg.getOptional("passfile");


        String username = null;
        if (usernameOpt.isPresent()) {
            username = usernameOpt.get();
        } else if (userfileOpt.isPresent()) {
            Path path = Paths.get(userfileOpt.get());
            try {
                username = Files.readAllLines(path).get(0);
            } catch (IOException e) {
                String error = "Error while reading username from file:" + path;
                logger.error(error, e);
                throw new RuntimeException(e);
            }
        }

        String password;
        if (username != null) {

            if (passwordOpt.isPresent()) {
                password = passwordOpt.get();
            } else if (passfileOpt.isPresent()) {
                Path path = Paths.get(passfileOpt.get());
                try {
                    password = Files.readAllLines(path).get(0);
                } catch (IOException e) {
                    String error = "Error while reading password from file:" + path;
                    logger.error(error, e);
                    throw new RuntimeException(e);
                }
            } else {
                String error = "username is present, but neither password nor passfile are defined.";
                logger.error(error);
                throw new RuntimeException(error);
            }
            builder.withAuthCredentials(username, password);
        }


        cfg.getOptional("whitelist").

            ifPresent(wl ->

            {
                List<InetSocketAddress> addrs = Arrays
                    .stream(wl.split(","))
                    .map(this::toSocketAddr)
                    .toList();
                builder.withNodeDistanceEvaluator(new NodeFilterToDistanceEvaluatorAdapter(n -> {
                    return (n.getBroadcastAddress().isPresent() && addrs.contains(n.getBroadcastAddress().get()))
                        || (n.getBroadcastRpcAddress().isPresent() && addrs.contains(n.getBroadcastRpcAddress().get()))
                        || (n.getListenAddress().isPresent() && addrs.contains(n.getListenAddress().get()));
                }));
            });

        cfg.getOptional("cloud_proxy_address").

            ifPresent(cpa ->

            {
                String[] addr = cpa.split(":", 2);
                if (addr.length == 1) {
                    throw new RuntimeException("cloud_proxy_address must be specified in host:port form.");
                }
                builder.withCloudProxyAddress(InetSocketAddress.createUnresolved(addr[0], Integer.parseInt(addr[1])));
            });

        NBConfiguration sslCfg = SSLKsFactory.get().getConfigModel().extractConfig(cfg);

        SSLContext ctx = SSLKsFactory.get().getContext(sslCfg);
        if (ctx != null) {
            builder.withSslContext(ctx);
        }

        builder.withConfigLoader(dcl);
//        for (String profileName : dcl.getInitialConfig().getProfiles().keySet()) {
//            logger.info("Installing load balancer observer in profile '" + profileName);
//            DriverExecutionProfile profile = dcl.getInitialConfig().getProfile(profileName);
//            String string = profile.getString(TypedDriverOption.LOAD_BALANCING_POLICY_CLASS.getRawOption());
//            dcl.getInitialConfig().getDefaultProfile(profileName).getp
//            Cqld4LoadBalancerObserver observer = new Cqld4LoadBalancerObserver(string);
//        }
//
//
//        builder.withNodeFilter()
        if (builder instanceof Cqld4SessionBuilder cqld4sb) {
            cqld4sb.setNodeSummarizer(diag);
        }
        CqlSession session = builder.build();
        return session;
    }

    private InetSocketAddress toSocketAddr(String addr) {
        String[] addrs = addr.split(":", 2);
        String inetHost = addrs[0];
        String inetPort = (addrs.length == 2) ? addrs[1] : "9042";
        return new InetSocketAddress(inetHost, Integer.valueOf(inetPort));
    }


    private Optional<DriverConfigLoader> resolveConfigLoader(NBConfiguration cfg) {
        Optional<String> maybeDriverConfig = cfg.getOptional("driverconfig");

        if (maybeDriverConfig.isEmpty()) {
            return Optional.empty();
        }

        String driverconfig = maybeDriverConfig.get();

        List<String> loaderspecs = NBConfigSplitter.splitConfigLoaders(driverconfig);
        LinkedList<DriverConfigLoader> loaders = new LinkedList<>();

        for (String loaderspec : loaderspecs) {
            // path
            Optional<Content<?>> fsconfig = NBIO.fs().pathname(driverconfig).first();
            if (fsconfig.isPresent()) {
                loaders.add(DriverConfigLoader.fromPath(fsconfig.get().asPath()));
                continue;
            }

            // classpath
            Optional<Content<?>> cpconfig = NBIO.classpath().pathname(driverconfig).first();
            if (cpconfig.isPresent()) {
                loaders.add(DriverConfigLoader.fromClasspath(driverconfig));
                continue;
            }

            // URLs
            try {
                Optional<Content<?>> remoteconf = NBIO.remote().pathname(driverconfig).first();
                if (remoteconf.isPresent()) {
                    loaders.add(DriverConfigLoader.fromUrl(remoteconf.get().getURI().toURL()));
                    continue;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // string form
            if ((driverconfig.contains(":") || driverconfig.contains("{"))
                && !driverconfig.contains(":\\/\\/")) {
                loaders.add(DriverConfigLoader.fromString(driverconfig));
                continue;
            }
        }

        if (loaders.size() == 0) {
            throw new RuntimeException("The driverconfig parameter was provided, but no loader could be found for '" + driverconfig + "'. Ensure files or URLs are accessible.");
        } else if (loaders.size() == 1) {
            return Optional.of(loaders.getFirst());
        } else {
            DriverConfigLoader mainloader = loaders.removeFirst();
            for (DriverConfigLoader loader : loaders) {
                mainloader = DriverConfigLoader.compose(mainloader, loader);
            }
            return Optional.of(mainloader);
        }

    }

    public CqlSession getSession() {
        return session;
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(Cqld4Space.class)
            .add(Param.optional("localdc"))
            .add(Param.optional(List.of("secureconnectbundle", "scb")))
            .add(Param.optional(List.of("hosts", "host")))
            .add(Param.defaultTo("port", 9042))
            .add(Param.optional("driverconfig", String.class))
            .add(Param.optional("username", String.class, "user name (see also password and passfile)"))
            .add(Param.optional("userfile", String.class, "file to load the username from"))
            .add(Param.optional("password", String.class, "password (see also passfile)"))
            .add(Param.optional("passfile", String.class, "file to load the password from"))
            .add(Param.optional("whitelist", String.class, "list of whitelist hosts addresses"))
            .add(Param.optional("showstmt", Boolean.class, "show the contents of the statement in the log"))
            .add(Param.optional("cloud_proxy_address", String.class, "Cloud Proxy Address"))
            .add(Param.optional("maxpages", Integer.class, "Maximum number of pages allowed per CQL request"))
            .add(Param.optional("maxretryreplace", Integer.class, "Maximum number of retry replaces with LWT for a CQL request"))
            .add(Param.defaultTo("diag", "none").setDescription("What level of diagnostics to report"))
            .add(SSLKsFactory.get().getConfigModel())
            .add(getDriverOptionsModel())
            .add(new OptionHelpers(new OptionsMap()).getConfigModel())
            .asReadOnly();
    }

    private static enum Diagnostics {
        queryplan
    }

    @Override
    public void close() {
        try {
            this.getSession().close();
        } catch (Exception e) {
            logger.warn("auto-closeable cql session threw exception in cql space(" + this.space + "): " + e);
            throw e;
        }
    }
}
