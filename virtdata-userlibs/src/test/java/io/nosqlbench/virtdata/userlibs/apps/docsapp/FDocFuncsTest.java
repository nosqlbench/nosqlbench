package io.nosqlbench.virtdata.userlibs.apps.docsapp;

import io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs.ExampleDocFunc1;
import io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs.ExampleDocFunc2;
import io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs.FDocFunc;
import io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs.FDocFuncs;
import org.junit.Test;

public class FDocFuncsTest {

    @Test
    public void testMarkdownFormat() {
        ExampleDocFunc1 exampleDocData1 = new ExampleDocFunc1();
        ExampleDocFunc2 exampleDocData2 = new ExampleDocFunc2();
        FDocFuncs funcs = new FDocFuncs(exampleDocData1.getClassName());

        funcs.addFunctionDoc(new FDocFunc(exampleDocData1));
        funcs.addFunctionDoc(new FDocFunc(exampleDocData2));

        String out = funcs.asMarkdown();
        System.out.print(out);
    }

}
