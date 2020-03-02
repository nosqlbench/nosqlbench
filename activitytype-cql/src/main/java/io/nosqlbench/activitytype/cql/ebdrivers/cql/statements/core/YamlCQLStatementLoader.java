package io.nosqlbench.activitytype.cql.ebdrivers.cql.statements.core;

import io.nosqlbench.engine.api.activityimpl.ActivityInitializationError;
import io.nosqlbench.engine.api.util.NosqlBenchFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class YamlCQLStatementLoader {

    private final static Logger logger = LoggerFactory.getLogger(YamlCQLStatementLoader.class);
    List<Function<String, String>> transformers = new ArrayList<>();

    public YamlCQLStatementLoader() {
    }

    public YamlCQLStatementLoader(Function<String, String>... transformers) {
        this.transformers.addAll(Arrays.asList(transformers));
    }

    public AvailableCQLStatements load(String fromPath, String... searchPaths) {

        InputStream stream = NosqlBenchFiles.findRequiredStreamOrFile(fromPath,
                "yaml", searchPaths);
        String data = "";
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
            data = buffer.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Error while reading yaml stream data:" + e);
        }

        for (Function<String, String> xform : transformers) {
            try {
                logger.debug("Applying string transformer to yaml data:" + xform);
                data = xform.apply(data);
            } catch (Exception e) {
                RuntimeException t = new ActivityInitializationError("Error applying string transform to input", e);
                logger.error(t.getMessage(), t);
                throw t;
            }
        }

        Yaml yaml = getCustomYaml();

        try {
            Iterable<Object> objects = yaml.loadAll(data);
            List<TaggedCQLStatementDefs> stmtListList = new ArrayList<>();
            for (Object object : objects) {
                TaggedCQLStatementDefs tsd = (TaggedCQLStatementDefs) object;
                stmtListList.add(tsd);
            }
            return new AvailableCQLStatements(stmtListList);

        } catch (Exception e) {
            logger.error("Error loading yaml from " + fromPath, e);
            throw e;
        }

    }

    private Yaml getCustomYaml() {
        Constructor constructor = new Constructor(TaggedCQLStatementDefs.class);
        TypeDescription tds = new TypeDescription(TaggedCQLStatementDefs.class);
        tds.putListPropertyType("statements", CQLStatementDef.class);
        constructor.addTypeDescription(tds);
        return new Yaml(constructor);
    }

}
