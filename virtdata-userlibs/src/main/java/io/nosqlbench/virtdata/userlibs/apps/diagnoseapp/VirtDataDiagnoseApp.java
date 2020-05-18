package io.nosqlbench.virtdata.userlibs.apps.diagnoseapp;

import io.nosqlbench.virtdata.core.bindings.ResolverDiagnostics;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import io.nosqlbench.virtdata.userlibs.apps.valuesapp.RunData;
import io.nosqlbench.virtdata.userlibs.apps.valuesapp.ValuesCheckerCoordinator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class VirtDataDiagnoseApp {

    private final static Logger logger  = LogManager.getLogger(VirtDataDiagnoseApp.class);

    public static void main(String[] args) {
        if (args.length==1) {
            diagnose(args[0]);
        } else {
            System.out.println(" ARGS: 'specifier'");
            System.exit(2);
        }

    }

    private static void diagnose(String mapperSpec) {
        ResolverDiagnostics diags = VirtData.getMapperDiagnostics(mapperSpec);
        System.out.println("mapper diagnostics:\n" + diags.toString());
    }

}
