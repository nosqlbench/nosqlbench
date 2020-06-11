package io.nosqlbench.engine.api.activityconfig.stmtloader;

import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsDoc;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsDocList;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.activityimpl.ActivityInitializationError;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.DiscreteProbabilityBuffer;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

public class GenericStmtLoader {

    List<Function<String, String>> stringTransformers = new ArrayList<>();
    private final ArrayList<Function<String,String>> transformers = new ArrayList<>();

    public void addTransformers(Function<String, String>... newTransformers) {
        Collections.addAll(this.transformers, newTransformers);
    }

    public RawStmtsDocList load(
            Logger logger,
            String path,
            String... searchPaths) {

        String data = null;
        try {
            Optional<Content<?>> oyaml = NBIO.all().prefix(searchPaths).name(path).extension("yaml").first();
            data = oyaml.map(Content::asString).orElseThrow(() -> new BasicError("Unable to load " + path));
        } catch (Exception e) {
            throw new RuntimeException("error while reading file " + path, e);
        }

        try {
            if (logger != null) logger.debug("Applying string transformer to yaml data:" + data);
            for (Function<String, String> transformer : transformers) {
                data = transformer.apply(data);
            }
        } catch (Exception e) {
            RuntimeException t = new ActivityInitializationError("Error applying string applyTransforms to input", e);
            if (logger != null) logger.error(t.getMessage(), t);
            throw t;
        }

        return parseYaml(logger, data);

    }

    private RawStmtsDocList parseYaml(Logger logger, String data) {
        Yaml yaml = new Yaml();
        Iterable<Object> objects = yaml.loadAll(data);
        List<RawStmtsDoc> newDocList = new ArrayList<>();

        for (Object object : objects) {
            if (object instanceof Map) {
                RawStmtsDoc doc = new RawStmtsDoc();
                doc.setFieldsByReflection((Map<String, Object>) object);
                newDocList.add(doc);
            } else {
                throw new RuntimeException("Unable to coerce a non-map type to a statements yaml doc: " + object.getClass().getCanonicalName());
            }
        }
        RawStmtsDocList rawStmtsDocList = new RawStmtsDocList(newDocList);
        return rawStmtsDocList;
    }

    protected String applyTransforms(Logger logger, String data) {
        for (Function<String, String> xform : stringTransformers) {
            try {
                if (logger != null) logger.debug("Applying string transformer to yaml data:" + xform);
                data = xform.apply(data);
            } catch (Exception e) {
                RuntimeException t = new ActivityInitializationError("Error applying string applyTransforms to input", e);
                if (logger != null) logger.error(t.getMessage(), t);
                throw t;
            }
        }
        return data;
    }

}
