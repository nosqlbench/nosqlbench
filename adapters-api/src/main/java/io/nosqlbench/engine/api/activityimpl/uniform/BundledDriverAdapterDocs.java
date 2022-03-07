package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.docapi.BundledMarkdownManifest;
import io.nosqlbench.docapi.Docs;
import io.nosqlbench.docapi.DocsBinder;
import io.nosqlbench.nb.annotations.Maturity;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.spi.SimpleServiceLoader;

import java.util.List;

@Service(value = BundledMarkdownManifest.class, selector = "adapter-docs")
public class BundledDriverAdapterDocs implements BundledMarkdownManifest {
    @Override
    public DocsBinder getDocs() {
        Docs docs = new Docs().namespace("adapter-docs");
        SimpleServiceLoader<DriverAdapter> loader = new SimpleServiceLoader<>(DriverAdapter.class, Maturity.Any);
        List<SimpleServiceLoader.Component<? extends DriverAdapter>> namedProviders = loader.getNamedProviders();
        for (SimpleServiceLoader.Component<? extends DriverAdapter> namedProvider : namedProviders) {
            DriverAdapter driverAdapter = namedProvider.provider.get();
            DocsBinder bundledDocs = driverAdapter.getBundledDocs();
            docs.merge(bundledDocs);
        }
        return docs;
    }
}
