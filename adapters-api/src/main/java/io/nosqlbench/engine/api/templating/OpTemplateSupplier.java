package io.nosqlbench.engine.api.templating;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;

import java.util.List;
import java.util.Optional;

/**
 * An Op Template Supplier can provide its own source of op templates instead
 * of relying on the built-in mechanism. By default, the built-in mechanism
 * will read op definitions from parameters first, then any ops (statements)
 * from yaml files provided in the workload= or yaml= activity parameters.
 */
public interface OpTemplateSupplier extends DriverAdapterDecorators {

    Optional<List<OpTemplate>> loadOpTemplates(NBConfiguration cfg);
}
