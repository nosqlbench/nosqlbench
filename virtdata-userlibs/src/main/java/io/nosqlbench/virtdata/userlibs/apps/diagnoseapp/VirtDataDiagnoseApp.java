package io.nosqlbench.virtdata.userlibs.apps.diagnoseapp;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
