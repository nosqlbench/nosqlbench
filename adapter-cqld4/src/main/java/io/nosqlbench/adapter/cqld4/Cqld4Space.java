package io.nosqlbench.adapter.cqld4;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DriverConfig;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.OptionsMap;
import com.datastax.oss.driver.api.core.config.TypedDriverOption;
import com.datastax.oss.driver.internal.core.config.composite.CompositeDriverConfigLoader;
import com.datastax.oss.driver.internal.core.loadbalancing.helper.NodeFilterToDistanceEvaluatorAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.adapter.cqld4.optionhelpers.OptionHelpers;
import io.nosqlbench.engine.api.util.SSLKsFactory;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Cqld4Space {
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
        CqlSessionBuilder builder = new CqlSessionBuilder();

        // stop insights for testing
        OptionsMap defaults = new OptionsMap();
        defaults.put(TypedDriverOption.MONITOR_REPORTING_ENABLED, false); // We don't need to do this every time we run a test or sanity check
        DriverConfigLoader dcl = DriverConfigLoader.fromMap(defaults);

        // add streamlined cql parameters
        OptionHelpers helpers = new OptionHelpers(defaults);
        NBConfiguration cqlHelperCfg = helpers.getConfigModel().extractConfig(cfg);
        helpers.applyConfig(cqlHelperCfg);

        // add user-provided parameters
        NBConfiguration driverCfg = getDriverOptionsModel().extractConfig(cfg);
        if (!driverCfg.isEmpty()) {
            Map<String, Object> remapped = new LinkedHashMap<>();
            driverCfg.getMap().forEach((k, v) -> remapped.put(k.substring("driver.".length()), v));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String remappedViaSerdesToSatisfyObtuseConfigAPI = gson.toJson(remapped);
            DriverConfigLoader userProvidedOptions = DriverConfigLoader.fromString(remappedViaSerdesToSatisfyObtuseConfigAPI);
            dcl = new CompositeDriverConfigLoader(dcl, userProvidedOptions);
        }

        // add referenced config from 'cfg' activity parameter
        DriverConfigLoader cfgDefaults = resolveConfigLoader(cfg).orElse(DriverConfigLoader.fromMap(OptionsMap.driverDefaults()));
        dcl = new CompositeDriverConfigLoader(dcl, cfgDefaults);

        builder.withConfigLoader(dcl);

        int port = cfg.getOrDefault("port", 9042);

        Optional<String> scb = cfg.getOptional(String.class, "secureconnectbundle", "scb");
        scb.flatMap(s -> NBIO.all().name(s).first().map(Content::getInputStream))
            .map(builder::withCloudSecureConnectBundle);

        Optional<List<InetSocketAddress>> contactPointsOption = cfg
            .getOptional("host", "hosts")
            .map(s -> Arrays.asList(s.split(",")))
            .map(
                sl -> sl.stream()
                    .map(n -> new InetSocketAddress(n, port))
                    .collect(Collectors.toList())
            );

        if (scb.isEmpty()) {
            if (contactPointsOption.isPresent()) {
                builder.addContactPoints(contactPointsOption.get());
                Optional<String> localdc = cfg.getOptional("localdc");
                builder.withLocalDatacenter(localdc.orElseThrow(
                    () -> new BasicError("Starting with driver 4.0, you must specify the local datacenter name with any specified contact points. Example: (use caution) localdc=datacenter1")
                ));
            } else {
                builder.addContactPoints(List.of(new InetSocketAddress("localhost", port)));
            }
        }

//        builder.withCompression(ProtocolOptions.Compression.NONE);
//
        Optional<String> usernameOpt = cfg.getOptional("username");
        Optional<String> passwordOpt = cfg.getOptional("password");
        Optional<String> passfileOpt = cfg.getOptional("passfile");

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
            builder.withAuthCredentials(username, password);
        }

        cfg.getOptional("whitelist").ifPresent(wl -> {
            List<InetSocketAddress> addrs = Arrays
                .stream(wl.split(","))
                .map(this::toSocketAddr)
                .toList();
            builder.withNodeDistanceEvaluator(new NodeFilterToDistanceEvaluatorAdapter(n -> {
                return (n.getBroadcastAddress().isPresent() && addrs.contains(n.getBroadcastAddress().get()))
                ||(n.getBroadcastRpcAddress().isPresent() && addrs.contains(n.getBroadcastRpcAddress().get()))
                    ||(n.getListenAddress().isPresent() && addrs.contains(n.getListenAddress().get()));
            }));
        });

        cfg.getOptional("cloud_proxy_address").ifPresent(cpa -> {
            String[] addr = cpa.split(":",2);
            if (addr.length==1) {
                throw new RuntimeException("cloud_proxy_address must be specified in host:port form.");
            }
            builder.withCloudProxyAddress(InetSocketAddress.createUnresolved(addr[0],Integer.parseInt(addr[1])));
        });

        NBConfiguration sslCfg = SSLKsFactory.get().getConfigModel().extractConfig(cfg);

        SSLContext ctx = SSLKsFactory.get().getContext(sslCfg);
        if (ctx != null) {
            builder.withSslContext(ctx);
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
            Optional<Content<?>> fsconfig = NBIO.fs().name(driverconfig).first();
            if (fsconfig.isPresent()) {
                loaders.add(DriverConfigLoader.fromPath(fsconfig.get().asPath()));
                continue;
            }

            // classpath
            Optional<Content<?>> cpconfig = NBIO.classpath().name(driverconfig).first();
            if (cpconfig.isPresent()) {
                loaders.add(DriverConfigLoader.fromClasspath(driverconfig));
                continue;
            }

            // URLs
            try {
                Optional<Content<?>> removeconf = NBIO.remote().name(driverconfig).first();
                if (removeconf.isPresent()) {
                    loaders.add(DriverConfigLoader.fromUrl(removeconf.get().getURI().toURL()));
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
            throw new RuntimeException("Unexpected size of loaders list:" + 0);
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
            .add(Param.optional("secureconnectbundle"))
            .add(Param.optional("hosts"))
            .add(Param.optional("driverconfig",String.class))
            .add(Param.optional("username",String.class,"user name (see also password and passfile)"))
            .add(Param.optional("password", String.class, "password (see also passfile)"))
            .add(Param.optional("passfile",String.class,"file to load the password from"))
            .add(Param.optional("whitelist",String.class,"list of whitelist hosts addresses"))
            .add(Param.optional("cloud_proxy_address",String.class,"Cloud Proxy Address"))
            .add(SSLKsFactory.get().getConfigModel())
            .add(getDriverOptionsModel())
            .asReadOnly();

    }

}
