package io.nosqlbench.nb.api.config.params;

import io.nosqlbench.nb.api.content.NBIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;

public class DataSources {

    private final static Logger logger = LogManager.getLogger(DataSources.class);

    private static final List<ConfigSource> sources = List.of(
        new MapBackedConfigSource(),
        new JsonConfigSource(),
        new ParamsParserSource(),
        new ListBackedConfigSource()
    );

    public static List<ElementData> elements(Object src) {

        if (src instanceof CharSequence && src.toString().startsWith("IMPORT{") && src.toString().endsWith("}")) {
            String data = src.toString();
            String filename = data.substring("IMPORT{".length(), data.length() - 1);
            Path filepath = Path.of(filename);

            src = NBIO.all().name(filename).first()
                .map(c -> {
                    logger.debug("found 'data' at " + c.getURI());
                    return c.asString();
                }).orElseThrow();
        }

        if (src instanceof ElementData) {
            return List.of((ElementData) src);
        }

        for (ConfigSource source : sources) {
            if (source.canRead(src)) {
                List<ElementData> elements = source.getAll(src);
                return elements;
            }
        }

        throw new RuntimeException("Unable to find a config reader for source type " + src.getClass().getCanonicalName());

    }

    public static ElementData element(Object object) {
        List<ElementData> elements = elements(object);
        if (elements.size() != 1) {
            throw new RuntimeException("Expected exactly one object, but found " + elements.size());
        }
        return elements.get(0);
    }
}
