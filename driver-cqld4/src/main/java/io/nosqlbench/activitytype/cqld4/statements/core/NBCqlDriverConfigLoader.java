package io.nosqlbench.activitytype.cqld4.statements.core;

import com.datastax.oss.driver.api.core.config.DriverOption;
import com.datastax.oss.driver.api.core.config.OptionsMap;
import com.datastax.oss.driver.internal.core.config.map.MapBasedDriverConfigLoader;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Map;

public class NBCqlDriverConfigLoader extends MapBasedDriverConfigLoader {

    public NBCqlDriverConfigLoader(
        @NonNull OptionsMap source,
        @NonNull Map<String, Map<DriverOption, Object>> rawMap) {
        super(source, rawMap);
    }

}
