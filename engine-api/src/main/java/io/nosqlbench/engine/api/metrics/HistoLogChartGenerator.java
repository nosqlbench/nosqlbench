package io.nosqlbench.engine.api.metrics;

/*
 *
 * @author Sebastián Estévez on 10/25/18.
 *
 */


import com.mitchtalmadge.asciidata.graph.ASCIIGraph;
import org.HdrHistogram.HistogramLogReader;
import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class HistoLogChartGenerator {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private static Map<String, ArrayList<Histogram>> histogramsOverTime = new HashMap<>();
    private static Logger logger = LoggerFactory.getLogger(HistoLogChartGenerator.class);

    public static void generateChartFromHistoLog(HistoIntervalLogger histoIntervalLogger) {
        File logFile = histoIntervalLogger.getLogfile();

        try {
            HistogramLogReader reader = new HistogramLogReader(logFile);

            while (reader.hasNext()){
                Histogram histogram = (Histogram)reader.nextIntervalHistogram();
                if (histogram != null) {
                    String tag = histogram.getTag();

                    ArrayList<Histogram> histogramList = histogramsOverTime.get(tag);
                    if (histogramList == null) {
                        histogramList = new ArrayList<>();
                    }
                    histogramList.add(histogram);
                    histogramsOverTime.put(tag, histogramList);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



        for (Map.Entry<String, ArrayList<Histogram>> p99KV : histogramsOverTime.entrySet()) {
            System.out.println(String.format("Charting p99 Latencies (in microseconds) over time (one second intervals) for %s:",p99KV.getKey()));
            double[] p99s = p99KV.getValue().stream().mapToDouble(x -> x.getValueAtPercentile(99)/1000).toArray(); //via method reference
            System.out.println("checking histogram length");
            System.out.flush();
            if (p99s.length < 2){
                System.out.println("Not enough data to chart");
                System.out.flush();
                continue;
            }else {
                System.out.println(ANSI_RED +
                        ASCIIGraph
                                .fromSeries(p99s)
                                .withNumRows(8)
                                .plot()
                        + ANSI_RESET);

                System.out.println(String.format("Charting throughput (number of transactions per second) for %s:", p99KV.getKey()));
                double[] rates = p99KV.getValue().stream().mapToDouble(x -> x.getTotalCount()).toArray(); //via method reference
                System.out.println(ANSI_GREEN +
                        ASCIIGraph
                                .fromSeries(rates)
                                .withNumRows(8)
                                .plot()
                        + ANSI_GREEN);
            }
        }
        System.out.println(ANSI_RESET);
    }
}
