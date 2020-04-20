package io.nosqlbench.engine.cli;

import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PathCanonicalizer {
    private final static Logger logger = LoggerFactory.getLogger(Cmd.class);

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
            if (!found.get().asPath().toString().equals(path)) {
                logger.info("rewrote path for " + path + " as " + found.get().asPath().toString());
                return found.get().asPath().toString();
            } else {
                logger.trace("kept path for " + path + " as " + found.get().asPath().toString());
            }
        } else {
            logger.trace("unable to find " + path + " for path qualification");
        }
        return path;
    }
}
