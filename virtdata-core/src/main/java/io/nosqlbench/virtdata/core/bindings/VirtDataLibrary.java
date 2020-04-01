package io.nosqlbench.virtdata.core.bindings;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public interface VirtDataLibrary {

    final static Logger logger = LogManager.getLogger(VirtDataLibrary.class);

    VirtDataFunctionLibrary getFunctionLibrary();
    String getLibname();

    BindingsTemplate getBindingsTemplate(String... namesAndSpecs);

}
