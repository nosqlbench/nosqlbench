package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplates;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.nb.api.tagging.TagFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

///  TODO: Auto-inject the default driver name into any op that doesn't have it set
///
/// ## Requirements for core
/// * All lookups must be lazy init
/// * All lookups must be cached
/// * All cache state must be extractable as plan
///
/// ## Requirements for callers
/// * Callers must be able to look up an op template by tag filter
/// * Callers must be able to look up a parsed op by tag filter
public class OpResolver {
    private OpTemplates opTemplates;
    private final Supplier<OpTemplates> optSupplier;
    List<DriverAdapter<CycleOp<?>, Space>> adapterlist = new ArrayList<>();

    public OpResolver(Supplier<OpTemplates> optSupplier) {
        this.optSupplier = optSupplier;
    }

    /// Find a required op template matching a tag filter
    public synchronized OpTemplate findOne(String tagFilter) {
        return findOneOptional(tagFilter).orElseThrow(
            () -> new OpConfigError("No op found for " + tagFilter));
    }

    private synchronized void load() {
        if (opTemplates==null) {
            opTemplates=optSupplier.get();
        }
    }

    /// Find an optional op template matching a tag filter
    public synchronized Optional<OpTemplate> findOneOptional(String tagFilter) {
        List<OpTemplate> matching = lookup(tagFilter);
        if (matching.size() > 1) {
            throw new OpConfigError(
                "Found more than one op templates with the tag filter: " + tagFilter);
        }
        return matching.size() == 1 ? Optional.of(matching.get(0)) : Optional.empty();
    }

    ///  Find any op templates matching a tag filter
    public synchronized List<OpTemplate> lookup(String tagFilter) {
        load();
        TagFilter tf = new TagFilter(tagFilter);
        return opTemplates.stream().filter(tf::matchesTagged).toList();
    }
}
