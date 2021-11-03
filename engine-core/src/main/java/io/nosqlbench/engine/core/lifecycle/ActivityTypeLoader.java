package io.nosqlbench.engine.core.lifecycle;

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.StandardActivityType;
import io.nosqlbench.nb.api.NBEnvironment;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.nb.api.spi.SimpleServiceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ActivityTypeLoader {

    private static final Logger logger = LogManager.getLogger(ActivityTypeLoader.class);
    private static final SimpleServiceLoader<ActivityType> ACTIVITYTYPE_SPI_FINDER = new SimpleServiceLoader<ActivityType>(ActivityType.class);
    private static final SimpleServiceLoader<DriverAdapter> DRIVERADAPTER_SPI_FINDER = new SimpleServiceLoader<>(DriverAdapter.class);
    private final Set<URL> jarUrls = new HashSet<>();

    public ActivityTypeLoader() {

        List<String> libpaths = NBEnvironment.INSTANCE.interpolateEach(":", "$" + NBEnvironment.NBLIBS);
        Set<URL> urlsToAdd = new HashSet<>();

        for (String libpaths_entry : libpaths) {
            Path libpath = Path.of(libpaths_entry);
            if (Files.isDirectory(libpath)) {
                urlsToAdd = addLibDir(urlsToAdd, libpath);
            } else if (Files.isRegularFile(libpath) && libpath.toString().toLowerCase().endsWith(".zip")) {
                urlsToAdd = addZipDir(urlsToAdd, libpath);
            } else if (Files.isRegularFile(libpath) && libpath.toString().toLowerCase().endsWith(".jar")) {
                urlsToAdd = addJarFile(urlsToAdd, libpath);
            }
        }
        extendClassLoader(urlsToAdd);
    }

    private synchronized void extendClassLoader(String... paths) {
        Set<URL> urls = new HashSet<>();
        for (String path : paths) {
            URL url = null;
            try {
                url = new URL(path);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            urls.add(url);
        }
        extendClassLoader(urls);
    }

    private synchronized void extendClassLoader(Set<URL> urls) {
        Set<URL> newUrls = new HashSet<>();
        if (!jarUrls.containsAll(urls)) {
            for (URL url : urls) {
                if (!jarUrls.contains(url)) {
                    newUrls.add(url);
                    jarUrls.add(url);
                }
            }
            URL[] newUrlAry = newUrls.toArray(new URL[]{});
            URLClassLoader ucl = URLClassLoader.newInstance(newUrlAry, Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(ucl);
            logger.debug("Extended class loader layering with " + newUrls);
        } else {
            logger.debug("All URLs specified were already in a class loader.");
        }
    }

    private Set<URL> addJarFile(Set<URL> urls, Path libpath) {
        try {
            urls.add(libpath.toUri().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return urls;
    }

    private Set<URL> addZipDir(Set<URL> urlsToAdd, Path libpath) {
        return urlsToAdd;
    }

    private Set<URL> addLibDir(Set<URL> urlsToAdd, Path libpath) {
        Set<URL> urls = NBIO.local()
            .prefix(libpath.toString())
            .extension(".jar")
            .list().stream().map(Content::getURL)
            .collect(Collectors.toSet());
        urlsToAdd.addAll(urls);
        return urlsToAdd;
    }

    public Optional<ActivityType> load(ActivityDef activityDef) {

        final String driverName = activityDef.getParams()
            .getOptionalString("driver", "type")
            .orElseThrow(() -> new BasicError("The parameter 'driver=' is required."));

        activityDef.getParams()
            .getOptionalString("jar")
            .map(jar -> {
                Set<URL> urls = NBIO.local().search(jar)
                    .list()
                    .stream().map(Content::getURL)
                    .collect(Collectors.toSet());
                return urls;
            })
            .ifPresent(this::extendClassLoader);

        return this.getDriverAdapter(driverName,activityDef)
            .or(() -> ACTIVITYTYPE_SPI_FINDER.getOptionally(driverName));

    }

    private Optional<ActivityType> getDriverAdapter(String activityTypeName, ActivityDef activityDef) {
        Optional<DriverAdapter> oda = DRIVERADAPTER_SPI_FINDER.getOptionally(activityTypeName);

        if (oda.isPresent()) {
            DriverAdapter<?, ?> driverAdapter = oda.get();

            activityDef.getParams().remove("driver");
            if (driverAdapter instanceof NBConfigurable) {
                NBConfigModel cfgModel = ((NBConfigurable) driverAdapter).getConfigModel();
                cfgModel = cfgModel.add(ACTIVITY_CFG_MODEL);
                NBConfiguration cfg = cfgModel.apply(activityDef.getParams());
                ((NBConfigurable) driverAdapter).applyConfig(cfg);
            }
            ActivityType activityType = new StandardActivityType<>(driverAdapter, activityDef);
            return Optional.of(activityType);
        } else {
            return Optional.empty();
        }
    }

    private static final NBConfigModel ACTIVITY_CFG_MODEL = ConfigModel.of(Activity.class)
        .add(Param.optional("threads").setRegex("\\d+|\\d+x|auto"))
        .add(Param.optional(List.of("workload", "yaml")))
        .add(Param.optional("cycles"))
        .add(Param.optional("alias"))
        .add(Param.optional(List.of("cyclerate", "rate")))
        .add(Param.optional("tags"))
        .asReadOnly();

    public Set<String> getAllSelectors() {
        List<String> allSelectors = ACTIVITYTYPE_SPI_FINDER.getAllSelectors();
        List<String> allDrivers = DRIVERADAPTER_SPI_FINDER.getAllSelectors();
        Set<String> all = new HashSet<>();
        all.addAll(allSelectors);
        all.addAll(allDrivers);
        return all;
    }
}
