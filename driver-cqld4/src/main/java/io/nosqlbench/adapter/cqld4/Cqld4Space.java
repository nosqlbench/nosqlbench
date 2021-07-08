package io.nosqlbench.adapter.cqld4;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.ParameterMap;
import io.nosqlbench.engine.api.util.SSLKsFactory;
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

    CqlSession session;

    public Cqld4Space(Cqld4DriverAdapter adapter) {
        session = createSession(adapter.getActivityDef());
    }

    private CqlSession createSession(ActivityDef activityDef) {
        CqlSessionBuilder builder = new CqlSessionBuilder();

        resolveConfigLoader(activityDef).ifPresent(builder::withConfigLoader);

        int port = activityDef.getParams().getOptionalInteger("port").orElse(9042);

        Optional<String> scb = activityDef.getParams().getOptionalString("secureconnectbundle");
        scb.flatMap(s -> NBIO.all().name(s).first().map(Content::getInputStream))
            .map(builder::withCloudSecureConnectBundle);

        Optional<List<InetSocketAddress>> contactPointsOption = activityDef.getParams()
            .getOptionalString("host", "hosts")
            .map(s -> Arrays.asList(s.split(",")))
            .map(
                sl -> sl.stream()
                    .map(n -> new InetSocketAddress(n, port))
                    .collect(Collectors.toList())
            );

        if (scb.isEmpty()) {
            builder.addContactPoints(
                contactPointsOption.orElse(List.of(new InetSocketAddress("localhost", port)))
            );
        }


//        builder.withCompression(ProtocolOptions.Compression.NONE);
//
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

        activityDef.getParams().getOptionalString("cbopts").ifPresent(
            e -> {
                throw new BasicError("this driver does not support option 'cbopts'");
            }
        );

        List.of(
            "cbopts",
            "whitelist",
            "lbp",
            "loadbalancingpolicy",
            "speculative",
            "protocol_version",
            "socketoptions",
            "reconnectpolicy",
            "pooling",
            "tickduration",
            "compression",
            "retrypolicy",
            "jmxreporting",
            "single-endpoint",
            "haproxy_source_ip"
        ).forEach(o -> {
            if (activityDef.getParams().getOptionalString(o).isPresent()) {
                String errmsg = "The activity parameter '" + o + "' is not supported in this version" +
                    " of the cqld4 driver as it was before in the cql (1.9) and cqld3 drivers. Note, you" +
                    " can often set these unsupported parameters in the driver configuration file directly." +
                    " If it should be supported as an activity parameter.please file an issue at http://nosqlbench.io/issues.";
                if (activityDef.getParams().getOptionalBoolean("ignore_warnings").orElse(false)) {
                    throw new BasicError(errmsg + " You can ignore this as a warning-only by setting ignore_warnings=true");
                } else {
                    logger.warn(errmsg + ", (ignored by setting ignore_warnings=true");
                }
            }
        });


        SSLContext context = SSLKsFactory.get().getContext(activityDef);
        if (context != null) {
            builder.withSslContext(context);
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

    private Optional<DriverConfigLoader> resolveConfigLoader(ActivityDef activityDef) {
        ParameterMap params = activityDef.getParams();
        String driverconfig = params.getOptionalString("driverconfig").orElse(null);
        if (driverconfig == null) {
            return Optional.empty();
        }
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

        if (loaders.size()==0) {
            throw new RuntimeException("Unexpected size of loaders list:" + 0);
        } else if (loaders.size()==1) {
            return Optional.of(loaders.getFirst());
        } else {
            DriverConfigLoader mainloader = loaders.removeFirst();
            for (DriverConfigLoader loader : loaders) {
                mainloader = DriverConfigLoader.compose(mainloader,loader);
            }
            return Optional.of(mainloader);
        }

    }

    public CqlSession getSession() {
        return session;
    }
}
