package io.virtdata.docsys.core;

import java.nio.file.Files;
import java.nio.file.Path;

public class DocServerApp {
    public final static String APPNAME_DOCSERVER = "docserver";
//    private final static Logger logger;

//    static {
//        // defer to an extant logger context if it is there, otherwise
//        // assume a local and docserver specific logging configuration
//
//        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
//        if (context.getLoggerList().size() == 1 && context.getLoggerList().get(0).getName().equals("ROOT")) {
//            configureDocServerLogging(context);
//            logger = LoggerFactory.getLogger(DocServerApp.class);
//            logger.info("Configured logging system from logback-docsys.xml");
//        } else {
//            logger = LoggerFactory.getLogger(DocServerApp.class);
//            logger.info("Configured logging within existing logging context.");
//        }
//    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].contains("help")) {
            showHelp();
        } else {
            runServer(args);
        }
    }

//    private static void configureDocServerLogging(LoggerContext context) {
//        JoranConfigurator jc = new JoranConfigurator();
//        jc.setContext(context);
//        context.reset();
//        context.putProperty("application-name", APPNAME_DOCSERVER);
//        InputStream is = DocServerApp.class.getClassLoader().getResourceAsStream("logback-docsys.xml");
//        if (is != null) {
//            try {
//                jc.doConfigure(is);
//            } catch (JoranException e) {
//                System.err.println("error initializing logging system: " + e.getMessage());
//                throw new RuntimeException(e);
//            }
//        } else {
//            throw new RuntimeException("No logging context was provided, and " +
//                    "logback-docsys.xml could not be loaded from the classpath.");
//        }
//    }

    private static void runServer(String[] serverArgs) {
        DocServer server = new DocServer();
        for (int i = 0; i < serverArgs.length; i++) {
            String arg = serverArgs[i];
            if (arg.matches(".*://.*")) {
                if (!arg.toLowerCase().contains("http://")) {
                    String suggested = arg.toLowerCase().replaceAll("https","http");
                    throw new RuntimeException("ERROR:\nIn this release, only 'http://' URLs are supported.\nTLS will be added in a future release.\nSee https://github.com/datastax/dsbench-labs/issues/29\n" +
                            "Consider using " + suggested);
                }
                server.withURL(arg);
            } else if (Files.exists(Path.of(arg))) {
                server.addPaths(Path.of(arg));
            } else if (arg.matches("\\d+")) {
                server.withPort(Integer.parseInt(arg));
            } else if (arg.matches("--public")) {
                server.withHost("0.0.0.0");
            }
        }
//
        server.run();
    }

    private static void showHelp(String... helpArgs) {
        System.out.println(
                "Usage: " + APPNAME_DOCSERVER + " " +
                        " [url] " +
                        " [path]... " + "\n" +
                        "\n" +
                        "If [url] is provided, then the scheme, address and port are all taken from it.\n" +
                        "Any additional paths are served from the filesystem, in addition to the internal ones.\n" +
                        "\n" +
                        "For now, only http:// is supported."
        );
    }

    private static void search(String[] searchArgs) {
    }

    private static void listTopics() {

    }
}
