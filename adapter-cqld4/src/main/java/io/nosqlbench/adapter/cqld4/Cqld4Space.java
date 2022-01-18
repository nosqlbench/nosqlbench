package io.nosqlbench.adapter.cqld4;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.*;
import com.datastax.oss.driver.internal.core.config.composite.CompositeDriverConfigLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.engine.api.util.SSLKsFactory;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Cqld4Space {
    private final static Logger logger = LogManager.getLogger(Cqld4Space.class);
    private final String space;

    CqlSession session;

    public Cqld4Space(String space,NBConfiguration cfg) {
        this.space = space;
        session = createSession(cfg);
    }

    private static NBConfigModel getDriverOptionsModel() {
        ConfigModel driverOpts = ConfigModel.of(DriverConfig.class);
        Iterable<TypedDriverOption<?>> builtins = TypedDriverOption.builtInValues();
        for (TypedDriverOption<?> builtin : builtins) {
            String path = builtin.getRawOption().getPath();
            Class<?> rawType = builtin.getExpectedType().getRawType();
            driverOpts.add(Param.optional("driver."+path,rawType));
        }
        return driverOpts.asReadOnly();
    }

    private CqlSession createSession(NBConfiguration cfg) {
        CqlSessionBuilder builder = new CqlSessionBuilder();

        // stop insights for testing
        OptionsMap defaults = new OptionsMap();
        defaults.put(TypedDriverOption.MONITOR_REPORTING_ENABLED, false); // We don't need to do this every time we run a test or sanity check
        DriverConfigLoader dcl = DriverConfigLoader.fromMap(defaults);

        // add user-provided parameters
        NBConfiguration driverCfg = getDriverOptionsModel().extractConfig(cfg);
        if (!driverCfg.isEmpty()) {
            Map<String,Object> remapped = new LinkedHashMap<>();
            driverCfg.getMap().forEach((k,v) -> remapped.put(k.substring("driver.".length()),v));
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


        NBConfiguration sslCfg = SSLKsFactory.get().getConfigModel().extractConfig(cfg);

        SSLContext ctx = SSLKsFactory.get().getContext(sslCfg);
        if (ctx != null) {
            builder.withSslContext(ctx);
        }

        CqlSession session = builder.build();
        return session;
    }


    /**
     * Split off any clearly separate config loader specifications from the beginning or end,
     * so they can be composed as an ordered set of config loaders.
     *
     * @param driverconfig The string containing driver config specs as described in the cqld4.md
     *                     documentation.
     * @return A list of zero or more strings, each representing a config source
     */
    // for testing
    public static List<String> splitConfigLoaders(String driverconfig) {
        List<String> configs = new ArrayList<>();
        Pattern preconfig = Pattern.compile("(?<pre>((\\w+://.+?)|[a-zA-z0-9_:'/\\\\]+?))\\s*,\\s*(?<rest>.+)");
        Matcher matcher = preconfig.matcher(driverconfig);
        while (matcher.matches()) {
            configs.add(matcher.group("pre"));
            driverconfig = matcher.group("rest");
            matcher = preconfig.matcher(driverconfig);
        }
        Pattern postconfig = Pattern.compile("(?<head>.+?)\\s*,\\s*(?<post>(\\w+://.+?)|([a-zA-Z0-9_:'/\\\\]+?))");
        matcher = postconfig.matcher(driverconfig);
        LinkedList<String> tail = new LinkedList<>();
        while (matcher.matches()) {
            tail.push(matcher.group("post"));
            driverconfig = matcher.group("head");
            matcher = postconfig.matcher(driverconfig);
        }
        if (!driverconfig.isEmpty()) {
            configs.add(driverconfig);
        }
        while (tail.size() > 0) {
            configs.add(tail.pop());
        }
        return configs;
    }

    private Optional<DriverConfigLoader> resolveConfigLoader(NBConfiguration cfg) {
        Optional<String> maybeDriverConfig = cfg.getOptional("driverconfig");

        if (maybeDriverConfig.isEmpty()) {
            return Optional.empty();
        }

        String driverconfig = maybeDriverConfig.get();

        List<String> loaderspecs = splitConfigLoaders(driverconfig);
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
            .add(Param.optional("driverconfig"))
            .add(Param.optional("username"))
            .add(Param.optional("password"))
            .add(Param.optional("passfile"))
            .add(SSLKsFactory.get().getConfigModel())
            .add(getDriverOptionsModel())
            .asReadOnly();

    }

}
