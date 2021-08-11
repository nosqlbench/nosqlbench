package io.nosqlbench.engine.core.lifecycle;

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.StandardActivityType;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ActivityTypeLoader {
    private static final Logger logger = LogManager.getLogger(ActivityTypeLoader.class);

    public static Optional<ActivityType<?>> load(ActivityDef activityDef) {

        String activityTypeName = activityDef.getParams().getOptionalString("driver", "type").orElse(null);

        List<String> knownTypes = ActivityType.FINDER.getAllSelectors();

        // Infer the type from either alias or yaml if possible (exactly one matches)
        if (activityTypeName == null) {
            List<String> matching = knownTypes.stream().filter(
                n ->
                    activityDef.getParams().getOptionalString("alias").orElse("").contains(n)
                        || activityDef.getParams().getOptionalString("yaml", "workload").orElse("").contains(n)
            ).collect(Collectors.toList());
            if (matching.size() == 1) {
                activityTypeName = matching.get(0);
                logger.info("param 'type' was inferred as '" + activityTypeName + "' since it was seen in yaml or alias parameter.");
            }
        }

        if (activityTypeName == null) {
            String errmsg = "You must provide a driver=<driver> parameter. Valid examples are:\n" +
                knownTypes.stream().map(t -> " driver=" + t + "\n").collect(Collectors.joining());
            throw new BasicError(errmsg);
        }

        String diagName = activityTypeName;


        Optional<ActivityType> ato = ActivityType.FINDER.getOptionally(activityTypeName);
        if (ato.isPresent()) {
            return Optional.of((ActivityType<?>) ato.get());
        }

        Optional<DriverAdapter> oda = StandardActivityType.FINDER.getOptionally(activityTypeName);

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
                throw new RuntimeException("Found neither ActivityType named '" + activityTypeName + "' nor DriverAdapter named '" + activityTypeName + "'.");
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

}
