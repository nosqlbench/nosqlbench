package io.nosqlbench.engine.cli;

import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.nio.file.FileSystems;
import java.util.Optional;

public class PathCanonicalizer {
    private final static Logger logger = LogManager.getLogger(Cmd.class);

    private final String[] includes;

    public PathCanonicalizer(String... includes) {
        this.includes = includes;
    }

    public String canonicalizePath(String path) {

        Optional<Content<?>> found = NBIO.local().prefix("activities")
            .prefix(includes)
            .name(path)
            .first();

        if (found.isPresent()) {
            String rewriteTo = found.get().asPath().toString();
            String separator = FileSystems.getDefault().getSeparator();
            rewriteTo=(rewriteTo.startsWith(separator) ? rewriteTo.substring(1) : rewriteTo);

            if (!rewriteTo.equals(path)) {
                if (NBIO.local().prefix("activities").prefix(includes).name(rewriteTo).first().isPresent()) {
                    logger.info("rewrote path for " + path + " as " + rewriteTo);
                    return rewriteTo;
                } else {
                    logger.trace("kept path for " + path + " as " + found.get().asPath().toString());
                    return path;
                }
            } else {
                logger.trace("kept path for " + path + " as " + found.get().asPath().toString());
            }
        } else {
            logger.trace("unable to find " + path + " for path qualification, either it is remote or missing.");
        }
        return path;
    }
}
