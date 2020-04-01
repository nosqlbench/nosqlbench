package io.nosqlbench.virtdata.core;

import io.nosqlbench.virtdata.api.VirtDataDocs;
import io.nosqlbench.virtdata.processors.DocFuncData;
import org.junit.Test;

import java.util.List;

public class VirtDataDocsIntegratedTest {

    @Test
    public void testGetAllNames() {
        List<String> allNames = VirtDataDocs.getAllNames();
    }

    @Test
    public void testGetAllDocs() {
        List<DocFuncData> allDocs = VirtDataDocs.getAllDocs();
    }
}
