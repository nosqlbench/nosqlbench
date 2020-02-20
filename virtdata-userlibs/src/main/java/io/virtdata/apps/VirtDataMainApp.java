package io.virtdata.apps;

import io.virtdata.apps.docsapp.AutoDocsApp;
import io.virtdata.apps.valuesapp.ValuesCheckerApp;
import io.virtdata.docsys.core.DocServerApp;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This just routes the user to the correct sub-app depending on the leading verb, stripping it off in the process.
 */
public class VirtDataMainApp {

    private final static String APP_TESTMAPPER = "testmapper";
    private final static String APP_GENDOCS = "gendocs";
    private final static String APP_DOCSERVER = "docserver";
    private final static String[] names = new String[]{APP_DOCSERVER, APP_GENDOCS, APP_TESTMAPPER};

    public static boolean hasNamedApp(String appname) {
        return (appname.equals(APP_TESTMAPPER)
        || appname.equals(APP_GENDOCS)
        || appname.equals(APP_DOCSERVER));
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: app (" + APP_TESTMAPPER + "|" + APP_GENDOCS + "|" + APP_DOCSERVER + ")");
            System.exit(0);
        }

        String appSelection = args[0];
        String[] appArgs = new String[0];
        if (args.length > 1) {
            appArgs = Arrays.copyOfRange(args, 1, args.length);
        }

        if (appSelection.toLowerCase().equals(APP_TESTMAPPER)) {
            ValuesCheckerApp.main(appArgs);
        } else if (appSelection.toLowerCase().equals(APP_GENDOCS)) {
            AutoDocsApp.main(appArgs);
        } else if (appSelection.toLowerCase().equals(APP_DOCSERVER)) {
            DocServerApp.main(appArgs);
        } else {
            System.err.println("Error in command line. The first argument must one of " + Arrays.stream(names).collect(Collectors.joining(",")));
        }
    }
}
