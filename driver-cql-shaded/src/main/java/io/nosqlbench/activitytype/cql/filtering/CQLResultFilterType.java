package io.nosqlbench.activitytype.cql.filtering;

import io.nosqlbench.activitytype.cql.errorhandling.CQLExceptionEnum;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.ResultFilterDispenser;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.ResultValueFilterType;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.tristate.EnumReadableMappingFilter;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.tristate.TristateFilter;
import io.nosqlbench.engine.api.util.ConfigTuples;
import io.nosqlbench.nb.annotations.Service;

import java.util.function.Predicate;

@Service(ResultValueFilterType.class)
public class CQLResultFilterType implements ResultValueFilterType {

    @Override
    public String getName() {
        return "cql";
    }

    @Override
    public ResultFilterDispenser getDispenser(String config) {
        return new Dispenser(config);
    }

    private class Dispenser implements ResultFilterDispenser {
        private final ConfigTuples conf;
        private final EnumReadableMappingFilter<CQLExceptionEnum> enumFilter;
        private final Predicate<ResultReadable> filter;

        public Dispenser(String config) {
            this.conf = new ConfigTuples(config);
            ConfigTuples inout = conf.getAllMatching("in.*", "ex.*");

            // Default policy is opposite of leading rule
            TristateFilter.Policy defaultPolicy = TristateFilter.Policy.Discard;
            if (conf.get(0).get(0).startsWith("ex")) {
                defaultPolicy = TristateFilter.Policy.Keep;
            }

            this.enumFilter =
                    new EnumReadableMappingFilter<>(CQLExceptionEnum.values(), TristateFilter.Policy.Ignore);

            for (ConfigTuples.Section section : inout) {
                if (section.get(0).startsWith("in")) {
                    this.enumFilter.addPolicy(section.get(1), TristateFilter.Policy.Keep);
                } else if (section.get(0).startsWith("ex")) {
                    this.enumFilter.addPolicy(section.get(1), TristateFilter.Policy.Discard);
                } else {
                    throw new RuntimeException("Section must start with in(clude) or ex(clude), but instead it is " + section);
                }

            }

            this.filter = this.enumFilter.toDefaultingPredicate(defaultPolicy);
        }

        @Override
        public Predicate<ResultReadable> getResultFilter() {
            return filter;
        }
    }

}
