package io.virtdata.apps.valuesapp;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ValuesCheckerApp {

    private final static Logger logger  = LogManager.getLogger(ValuesCheckerApp.class);public static void main(String[] args) {
        if (args.length<5) {
            System.out.println("ARGS: 'specifier' threads bufsize start end");
            System.out.println("example: 'timeuuid()' 100 1000 0 10000");
            System.out.println(" specifier: A VirtData function specifier.");
            System.out.println(" threads: The number of concurrent threads to run.");
            System.out.println(" bufsize: The number of cycles to give each thread at a time.");
            System.out.println(" start: The start cycle for the test, inclusive.");
            System.out.println(" end: The end cycle for the test, exclusive.");
            System.exit(2);
        }
        String spec = args[0];
        int threads = Integer.valueOf(args[1]);
        int bufsize = Integer.valueOf(args[2]);
        long start = Long.valueOf(args[3]);
        long end = Long.valueOf(args[4]);

        boolean isolated = false;

        if (args.length==6) {
            isolated=args[5].toLowerCase().equals("isolated") || args[5].toLowerCase().equals("true");
        }

        ValuesCheckerCoordinator checker = new ValuesCheckerCoordinator(spec, threads, bufsize, start, end, isolated);

        if (!isolated) {
            logger.warn("You are testing functions which are not intended to be thread-safe in a non-threadsafe way.");
            logger.warn("This is only advisable if you are doing development against the core libraries.");
            logger.warn("Results may vary.");
        }

        RunData runData;
        try {
            runData = checker.call();
            System.out.println(runData.toString());
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

    }

}
