package io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FDoc implements Iterable<FDocCat> {

    private final Map<String, FDocCat> docs = new HashMap<>();

    public FDoc() {
    }

    public FDocCat addCategory(String categoryName) {
        return docs.computeIfAbsent(categoryName, FDocCat::new);
    }

    @Override
    public Iterator<FDocCat> iterator() {
        return docs.values().iterator();
    }
}
