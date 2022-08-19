/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.virtdata.userlibs.apps.valuechecker;

import io.nosqlbench.virtdata.core.bindings.ResolverDiagnostics;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VirtDataCheckPerfApp {

    private final static Logger logger  = LogManager.getLogger(VirtDataCheckPerfApp.class);

    public static void main(String[] args) {
        if (args.length==1) {
            checkperf(new String[]{args[0],"1","1","1","1"});
        } else if (args.length==5) {
            checkperf(args);
        } else {
            System.out.println(" ARGS: checkperf 'specifier' threads bufsize start end");
            System.out.println(" example: 'timeuuid()' 100 1000 0 10000");
            System.out.println("  specifier: A VirtData function specifier.");
            System.out.println("  threads: The number of concurrent threads to run.");
            System.out.println("  bufsize: The number of cycles to give each thread at a time.");
            System.out.println("  start: The start cycle for the test, inclusive.");
            System.out.println("  end: The end cycle for the test, exclusive.");
            System.out.println(" OR");
            System.out.println(" ARGS: diagnose 'specifier'");
        }
    }

    private static void checkperf(String[] args) {
        String spec = args[0];
        int threads = Integer.parseInt(args[1]);
        int bufsize = Integer.parseInt(args[2]);
        long start = Long.parseLong(args[3]);
        long end = Long.parseLong(args[4]);

        boolean isolated = false;

        ValuesCheckerCoordinator checker = new ValuesCheckerCoordinator(spec, threads, bufsize, start, end, isolated);

        RunData runData;
        try {
            runData = checker.call();
            System.out.println(runData.toString());
        } catch (Exception e) {
            throw new RuntimeException("Error while checking performance: " + e, e);
        }

    }

    private static void diagnose(String[] args) {
        String mapperSpec = args[0];

        ResolverDiagnostics diags = VirtData.getMapperDiagnostics(mapperSpec);
        System.out.println("mapper diagnostics:\n" + diags.toString());

    }

}
