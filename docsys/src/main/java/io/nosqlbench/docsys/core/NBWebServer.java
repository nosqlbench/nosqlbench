package io.nosqlbench.docsys.core;

import io.nosqlbench.docsys.DocsysDefaultAppPath;
import io.nosqlbench.docsys.api.Docs;
import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.docsys.handlers.FavIconHandler;
import io.nosqlbench.nb.api.spi.SimpleServiceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.DispatcherType;
import javax.servlet.ServletRegistration;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.AccessMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * For examples, see <a href="https://git.eclipse.org/c/jetty/org.eclipse.jetty.project.git/tree/examples/embedded/src/main/java/org/eclipse/jetty/embedded/">embedded examples</a>
 */
public class NBWebServer implements Runnable {

    private final static Logger logger = LogManager.getLogger(NBWebServer.class);

    private final List<Path> basePaths = new ArrayList<>();
    private final List<Class> servletClasses = new ArrayList<>();
    private ServletContextHandler contextHandler;
    private ServletHolder servletHolder;
    private HandlerList handlers;

    private String bindScheme = "http";
    private String bindHost = "localhost";
    private int bindPort = 12345;

    private final Map<String, Object> contextParams = new LinkedHashMap<>();

    public NBWebServer withContextParams(Map<String, Object> cp) {
        this.contextParams.putAll(cp);
        return this;
    }

    public NBWebServer withContextParam(String name, Object object) {
        this.contextParams.put(name, object);
        return this;
    }

    public NBWebServer withHost(String bindHost) {
        this.bindHost = bindHost;
        return this;
    }

    public NBWebServer withPort(int bindPort) {
        this.bindPort = bindPort;
        return this;
    }

    public NBWebServer withURL(String urlSpec) {
        try {
            URL url = new URL(urlSpec);
            this.bindPort = url.getPort();
            this.bindHost = url.getHost();
            this.bindScheme = url.getProtocol();
            if (url.getPath() != null && !url.getPath().equals("/") && !url.getPath().equals("")) {
                throw new UnsupportedOperationException("You may not specify a path for the hosting URL. (specified '" + url.getPath() + "')");
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public NBWebServer withScheme(String scheme) {
        this.bindScheme = scheme;
        return this;
    }

    private void addWebObject(Class<?>... objects) {
        servletClasses.addAll(Arrays.asList(objects));
//        String servletClasses = this.servletClasses
//                .stream()
//                .map(Class::getCanonicalName)
//                .collect(Collectors.joining(","));
//
//        getServletHolder().setInitParameter(
//                "jersey.config.server.provider.classnames",
//                servletClasses
//        );
//        return this;
    }

    private void loadDynamicEndpoints() {
        List<Pattern> includeApps = List.of(Pattern.compile(".*"));
        if (contextParams.containsKey("include-apps")) {
            includeApps = Arrays.asList(contextParams.get("include-apps").toString().split(", *"))
                .stream()
                .map(Pattern::compile)
                .collect(Collectors.toList());
        }

        SimpleServiceLoader<WebServiceObject> svcLoader = new SimpleServiceLoader<>(WebServiceObject.class);
        svcLoader.getNamedProviders().values()
            .forEach(p -> {
                Class<? extends WebServiceObject> c = p.type();
                logger.info("Adding web service object: " + c.getSimpleName());
                this.addWebObject(c);
            });

        logger.debug("Loaded " + this.servletClasses.size() + " root resources.");

    }

    private ServletContextHandler getContextHandler() {
        if (contextHandler == null) {
            contextHandler = new ServletContextHandler();
            contextHandler.setContextPath("/*");
        }
        return contextHandler;
    }

    private ServletHolder getServletHolder() {
        if (servletHolder == null) {
            servletHolder = getContextHandler().addServlet(
                ServletContainer.class,
                "/apps"
            );
            servletHolder.setInitOrder(0);
        }
        return servletHolder;
    }

    public NBWebServer addPaths(Path... paths) {
        for (Path path : paths) {
            try {
                path.getFileSystem().provider().checkAccess(path, AccessMode.READ);
                this.basePaths.add(path);
            } catch (Exception e) {
                logger.error("Unable to access path " + path.toString());
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    public void run() {

        //new InetSocketAddress("")
        Server server = new Server(bindPort);
        handlers = new HandlerList();

        if (this.basePaths.size() == 0 && this.servletClasses.size() == 0) {
            logger.warn("No service endpoints or doc paths have been added. Loading dynamically.");
        }

        RewriteHandler rh = new RewriteHandler();
//        rh.addRule(new RedirectRegexRule("/","/docs/"));
//        rh.addRule(new RedirectPatternRule("/","/docs/"));
        handlers.addHandler(rh);
//        ShutdownHandler shutdownHandler; // for easy recycles

        // Favicon
        for (Path basePath : basePaths) {
            Path icon = basePath.resolve("/favicon.ico");
            if (Files.exists(icon)) {
                FavIconHandler favIconHandler = new FavIconHandler(basePaths.get(0) + "/favicon.ico", false);
                handlers.addHandler(favIconHandler);
                break;
            }
        }

        if (basePaths.size() == 0) {
            Docs docs = new Docs();
            // Load static path contexts which are published within the runtime.
            docs.merge(DocsysPathLoader.loadStaticPaths());

            // If none claims the "docsys-app" namespace, then install the
            // default static copy of the docs app
            if (!docs.getPathMap().containsKey("docsys-app")) {
                docs.merge(new DocsysDefaultAppPath().getDocs());
            }
            basePaths.addAll(docs.getPaths());
        }

        for (Path basePath : basePaths) {
            logger.info("Adding path to server: " + basePath.toString());
            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setDirAllowed(true);
            resourceHandler.setAcceptRanges(true);

            resourceHandler.setWelcomeFiles(new String[]{"index.html"});
            resourceHandler.setRedirectWelcome(false);
            Resource baseResource = new PathResource(basePath);

            if (basePath.toUri().toString().startsWith("jar:")) {
                try {
                    baseResource = JarResource.newResource(basePath.toUri());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }

            resourceHandler.setBaseResource(baseResource);
            resourceHandler.setCacheControl("no-cache");
            handlers.addHandler(resourceHandler);

        }

//        ResourceConfig statusResourceCfg = new ResourceConfig(DocServerStatusEndpoint.class);
//        statusResourceCfg.property("server", this);
//        ServletContainer statusResourceContainer = new ServletContainer(statusResourceCfg);
//        ServletHolder statusResourceServletHolder = new ServletHolder(statusResourceContainer);
//        getContextHandler().addServlet(statusResourceServletHolder, "/_");

        logger.info("adding " + servletClasses.size() + " context handlers...");
        loadDynamicEndpoints();


        ResourceConfig rc = new ResourceConfig();
        rc.addProperties(contextParams);
        rc.property("server", this);

        ServletContainer container = new ServletContainer(rc);
        ServletHolder servlets = new ServletHolder(container);
        String classnames = this.servletClasses
            .stream()
            .map(Class::getCanonicalName)
            .collect(Collectors.joining(","));
        rc.property(ServerProperties.PROVIDER_CLASSNAMES, classnames);
//        servlets.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES,
//                classnames
//        );
        ServletContextHandler sch = new ServletContextHandler();
        sch.setContextPath("/*");
        sch.addServlet(servlets, "/*");

        FilterHolder filter = new FilterHolder();
        filter.setInitParameter("allowedOrigins", "*");
        filter.setInitParameter("allowedMethods", "POST,GET,OPTIONS,PUT,DELETE,HEAD");
        filter.setInitParameter("allowedHeaders", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
        filter.setInitParameter("preflightMaxAge", "1800");
        filter.setInitParameter("allowCredentials", "true");

        CrossOriginFilter corsFilter = new CrossOriginFilter();
        filter.setFilter(corsFilter);

        FilterMapping filterMapping = new FilterMapping();
        filterMapping.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        filterMapping.setPathSpec("/*");
        filterMapping.setServletName("cross-origin");

        sch.addFilter(filter, "/*", EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC));

        handlers.addHandler(sch);

        // Show contexts
        DefaultHandler defaultHandler = new DefaultHandler();
        defaultHandler.setShowContexts(true);
        defaultHandler.setServeIcon(false);

        handlers.addHandler(defaultHandler);


//        FilterMapping corsMapping = new FilterMapping();
//        corsMapping.set
//
//        ServletHandler CorsHandler = new ServletHandler();
//        CorsHandler.setH

        server.setHandler(handlers);
        for (Connector connector : server.getConnectors()) {
            if (connector instanceof AbstractConnector) {
                logger.debug("Setting idle timeout for " + connector.toString() + " to 300,000ms");
                ((AbstractConnector) connector).setIdleTimeout(300000);
            }
        }
        try {
            List<Connector> connectors = new ArrayList<>();

            if (bindScheme.equals("http")) {
                ServerConnector sc = new ServerConnector(server);
                sc.setPort(bindPort);
                sc.setHost(bindHost);
//                sc.setDefaultProtocol(bindScheme);
                connectors.add(sc);
            } else if (bindScheme.equals("https")) {
                SslContextFactory.Server server1 = new SslContextFactory.Server();
                ServerConnector sc = new ServerConnector(server, server1);
                sc.setPort(bindPort);
                sc.setHost(bindHost);
//                sc.setDefaultProtocol(bindScheme);
                connectors.add(sc);
            }

            server.setConnectors(connectors.toArray(new Connector[0]));

            server.start();

            System.out.println("Started documentation server at " + bindScheme + "://" + bindHost + ":" + bindPort + "/");

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                System.out.println("Browsing to documentation server at " + bindScheme + "://" + bindHost + ":" + bindPort + "/");
                Desktop.getDesktop().browse(new URI(bindScheme + "://" + bindHost + ":" + bindPort + "/"));
                System.out.println("If the docs app did not open automatically in your browser, open to the the url above.");
            }

            server.join();
        } catch (Exception e) {
            logger.error("error while starting doc server", e);
            e.printStackTrace(System.out);
            System.exit(2);
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Class servletClass : this.servletClasses) {
            sb.append("- ").append(servletClass.getCanonicalName()).append("\n");
            ResourceConfig rc = new ResourceConfig(servletClass);
            Set<org.glassfish.jersey.server.model.Resource> resources = rc.getResources();
            for (org.glassfish.jersey.server.model.Resource resource : resources) {
                sb.append("resource name:").append(resource.getName()).append("\n");
                List<ResourceMethod> resourceMethods = resource.getResourceMethods();
                for (ResourceMethod resourceMethod : resourceMethods) {
                    String rm = resourceMethod.toString();
                    sb.append("rm:").append(rm);
                }
            }
        }
        sb.append("- - - -\n");
        for (Handler handler : handlers.getHandlers()) {
            String summary = handlerSummary(handler);
            sb.append(summary == null ? "NULL SUMMARY" : summary);
            sb.append("\n");
        }
        return sb.toString();
    }

    private String handlerSummary(Handler handler) {
        StringBuilder sb = new StringBuilder();
        sb.append("----> handler type ").append(handler.getClass().getSimpleName()).append("\n");

        if (handler instanceof ResourceHandler) {
            ResourceHandler h = (ResourceHandler) handler;
            sb.append(" base resource: ").append(h.getBaseResource().toString())
                .append("\n");
            sb.append(h.dump());
        } else if (handler instanceof ServletContextHandler) {
            ServletContextHandler h = (ServletContextHandler) handler;
            sb.append(h.dump()).append("\n");
            h.getServletContext().getServletRegistrations().forEach(
                (k, v) -> {
                    sb.append("==> servlet type ").append(k).append("\n");
                    sb.append(getServletSummary(v)).append("\n");
                }
            );
            sb.append("context path:").append(h.getContextPath());
        } else if (handler instanceof DefaultHandler) {
            DefaultHandler h = (DefaultHandler) handler;
            sb.append(h.dump());
        }
        return sb.toString();
    }

    private String getServletSummary(ServletRegistration v) {
        return v.getClassName() + "('" + v.getName() + "')" + v.getInitParameters().keySet().stream().map(
            k -> k + "=" + v.getInitParameters().get(k)).collect(Collectors.joining(","));
    }
}
