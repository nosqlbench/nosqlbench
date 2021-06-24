package io.nosqlbench.engine.api.templating;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import io.nosqlbench.virtdata.core.templates.CapturePoint;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * Parse an OpTemplate into a ParsedCommand, which can dispense object maps
 */
public class ParsedCommand implements LongFunction<Map<String, ?>> {

    private final static Logger logger = LogManager.getLogger(ParsedCommand.class);

    /**
     * the name of this operation
     **/
    private final String name;

    /**
     * The fields which are statically assigned
     **/
    private final Map<String, Object> statics = new LinkedHashMap<>();

    /**
     * The fields which are dynamic, and must be realized via functions.
     * This map contains keys which identify the field names, and values, which may be null or undefined.
     */
    private final Map<String, LongFunction<?>> dynamics = new LinkedHashMap<>();

    /**
     * The names of payload values in the result of the operation which should be saved.
     * The keys in this map represent the name of the value as it would be found in the native
     * representation of a result. If the values are defined, then each one represents the name
     * that the found value should be saved as instead of the original name.
     */
    private final List<List<CapturePoint>> captures = new ArrayList<>();
    private final int mapsize;

    /**
     * Create a parsed command from an Op template. The op template is simply the normalized view of
     * op template structure which is uniform regardless of the original format.
     *
     * @param ot An OpTemplate representing an operation to be performed in a native driver.
     */
    public ParsedCommand(OpTemplate ot) {
        this(ot, List.of());
    }

    public ParsedCommand(OpTemplate ot, List<Function<Map<String, Object>, Map<String, Object>>> preprocessors) {
        this.name = ot.getName();

        Map<String, Object> map = ot.getOp().orElseThrow();
        for (Function<Map<String, Object>, Map<String, Object>> preprocessor : preprocessors) {
            map = preprocessor.apply(map);
        }

        map.forEach((k, v) -> {
            if (v instanceof CharSequence) {
                ParsedTemplate pt = ParsedTemplate.of(((CharSequence) v).toString(), ot.getBindings());
                this.captures.add(pt.getCaptures());
                switch (pt.getType()) {
                    case literal:
                        statics.put(k, ((CharSequence) v).toString());
                        break;
                    case bindref:
                        String spec = pt.asBinding().orElseThrow().getBindspec();
                        Optional<DataMapper<Object>> mapper = VirtData.getOptionalMapper(spec);
                        dynamics.put(k, mapper.orElseThrow());
                        break;
                    case concat:
                        StringBindings sb = new StringBindings(pt);
                        dynamics.put(k, sb);
                        break;
                }
            } else {
                statics.put(k, v);
            }
        });

        mapsize = statics.size() + dynamics.size();


    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getStatics() {
        return statics;
    }

    public Map<String, LongFunction<?>> getDynamics() {
        return dynamics;
    }

    @Override
    public Map<String, Object> apply(long value) {
        HashMap<String,Object> map = new HashMap<>(mapsize);
        map.putAll(statics);
        dynamics.forEach((k,v) -> {
            map.put(k,v.apply(value));
        });
        return map;
    }

    public boolean isStatic(String prepared) {
        return statics.containsKey(prepared);
    }

    public <T> T getStatic(String prepared, Class<T> classOfT) {
        return (T) statics.get(prepared);
    }

    public <T> T get(String fieldName, long input) {
        if (statics.containsKey(fieldName)) {
            return (T) statics.get(fieldName);
        }
        if (dynamics.containsKey(fieldName)) {
            return (T) dynamics.get(fieldName).apply(input);
        }
        return null;
    }
}
