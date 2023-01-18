/*
 * Copyright (c) 2022-2023 nosqlbench
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

import java.util.Arrays;

public class VirtDataCheckPerfApp {

    private final static Logger logger  = LogManager.getLogger(VirtDataCheckPerfApp.class);

    public static void main(String[] args) {
        String spec="Identity()";
        int threads=1;
        int bufsize=1;
        long startCycle=0;
        long endCycle=1;
        boolean printValues=false;

        if ((args.length>0) && args[args.length-1].equals("-p")) {
            printValues=true;
            args= Arrays.copyOfRange(args,0,args.length-1);
        }

        switch (args.length) {
            case 5:
                endCycle=Integer.parseInt(args[4]);
            case 4:
                startCycle=Integer.parseInt(args[3]);
            case 3:
                bufsize=Integer.parseInt(args[2]);
            case 2:
                threads = Integer.parseInt(args[1]);
            case 1:
                spec = args[0];
                break;
            case 0:
                System.out.println(" ARGS: virtdata testmapper 'specifier' threads bufsize start end");
                System.out.println(" example: 'timeuuid()' 100 1000 0 10000");
                System.out.println("  specifier: A VirtData function specifier.");
                System.out.println("  threads: The number of concurrent threads to run.");
                System.out.println("  bufsize: The number of cycles to give each thread at a time.");
                System.out.println("  start: The start cycle for the test, inclusive.");
                System.out.println("  end: The end cycle for the test, exclusive.");
                System.out.println("  [-p]: print the values as a sanity check. (must appear last)");
                break;
            default:
                throw new RuntimeException("Error parsing args for " + String.join(" ",args));
        }

        checkperf(spec,threads,bufsize,startCycle,endCycle,printValues);
    }

    private static void checkperf(String spec, int threads, int bufsize, long start, long end, boolean printValues) {
        ValuesCheckerCoordinator checker = new ValuesCheckerCoordinator(spec, threads, bufsize, start, end, printValues);

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
