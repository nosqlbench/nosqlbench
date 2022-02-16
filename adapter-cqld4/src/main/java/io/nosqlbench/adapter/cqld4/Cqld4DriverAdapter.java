package io.nosqlbench.adapter.cqld4;

import io.nosqlbench.adapter.cqld4.opmappers.Cqld4CoreOpMapper;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service(value = DriverAdapter.class, selector = "cqld4")
public class Cqld4DriverAdapter extends BaseDriverAdapter<Op, Cqld4Space> {
    private final static Logger logger = LogManager.getLogger(Cqld4DriverAdapter.class);

    @Override
    public OpMapper<Op> getOpMapper() {
        DriverSpaceCache<? extends Cqld4Space> spaceCache = getSpaceCache();
        NBConfiguration config = getConfiguration();
        return new Cqld4CoreOpMapper(config, spaceCache);
    }

    @Override
    public Function<String, ? extends Cqld4Space> getSpaceInitializer(NBConfiguration cfg) {
        return s -> new Cqld4Space(s,cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return super.getConfigModel().add(Cqld4Space.getConfigModel());
    }


    @Override
    public List<Function<Map<String, Object>, Map<String, Object>>> getOpFieldRemappers() {
        List<Function<Map<String, Object>, Map<String, Object>>> remappers = new ArrayList<>();
        remappers.addAll(super.getOpFieldRemappers());

        // Simplify to the modern form and provide a helpful warning to the user
        // This auto updates to 'simple: <stmt>' or 'prepared: <stmt>' for cql types
        remappers.add(m -> {
            Map<String,Object> map = new LinkedHashMap<>(m);

            if (map.containsKey("stmt")) {
                String type = map.containsKey("type") ? map.get("type").toString() : "cql";
                if (type.equals("cql")){
                    boolean prepared = (!map.containsKey("prepared")) || map.get("prepared").equals(true);
                    map.put(prepared?"prepared":"simple",map.get("stmt"));
                    map.remove("stmt");
                    map.remove("type");
                }
            }

            return map;
        });

        return remappers;
    }
}
