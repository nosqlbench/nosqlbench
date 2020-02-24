package io.nosqlbench.engine.api.metrics;

import com.codahale.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ClassicHistoListener extends CapabilityHook<HistogramAttachment> {
    private final static Logger logger = LoggerFactory.getLogger(ClassicHistoListener.class);

    private final MetricRegistry metricsRegistry;
    private String sessionName;
    private String prefix;
    private Pattern pattern;
    private String interval;
    private TimeUnit nanoseconds;
    private Map<String, Attachment> histos = new HashMap<>();

    public ClassicHistoListener(MetricRegistry metricRegistry, String sessionName, String prefix, Pattern pattern, String interval, TimeUnit nanoseconds) {
        this.metricsRegistry = metricRegistry;
        this.sessionName = sessionName;
        this.prefix = prefix;
        this.pattern = pattern;
        this.interval = interval;
        this.nanoseconds = nanoseconds;
    }

    @Override
    public void onCapableAdded(String name, HistogramAttachment capable) {
        if (pattern.matcher(name).matches()) {
            String prefixed = prefix + "-" + name;
            Histogram classicHisto = new Histogram(new ExponentiallyDecayingReservoir());
            capable.attachHistogram(classicHisto);

            this.histos.put(prefixed, new Attachment(name, prefix, capable, classicHisto));
            metricsRegistry.histogram(prefixed, () -> classicHisto);
            logger.trace("Added classic histogram attachment:" + prefixed);
        }

    }

    @Override
    public void onCapableRemoved(String name, HistogramAttachment capable) {
        Attachment removed = histos.remove(name);
        logger.trace("Removed classic histogram attachment: " + removed);
    }

    @Override
    protected Class<HistogramAttachment> getCapabilityClass() {
        return HistogramAttachment.class;
    }

    private static class Attachment {

        public final String upstreamName;
        public final HistogramAttachment upstream;
        public Histogram downstream;
        public String prefix;

        public Attachment(String upstreamName, String prefix, HistogramAttachment upstream, Histogram downstream) {
            this.prefix = prefix;
            this.upstreamName = upstreamName;
            this.upstream = upstream;
            this.downstream = downstream;
        }

        public String toString() {
            return "name:" + upstreamName + "->" + prefix + "-" + upstreamName;
        }

        public String getName() {
            return upstreamName;
        }
    }
}
