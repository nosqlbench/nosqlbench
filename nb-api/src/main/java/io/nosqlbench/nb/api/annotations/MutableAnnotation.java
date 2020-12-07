package io.nosqlbench.nb.api.annotations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import io.nosqlbench.nb.api.Layer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class MutableAnnotation implements Annotation {

    private final static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private String session = "SESSION_UNNAMED";

    @Expose
    private Layer layer;

    @Expose
    private long start = 0L;

    @Expose
    private long end = 0L;

    @Expose
    private Map<String, String> labels = new LinkedHashMap<>();

    @Expose
    private Map<String, String> details = new LinkedHashMap<>();

    private final ZoneId zoneid = ZoneId.of("GMT");

    public MutableAnnotation(
            TimeZone timezone,
            String session,
            Layer layer,
            long start,
            long end,
            LinkedHashMap<String, String> labels,
            LinkedHashMap<String, String> details) {
        setLabels(labels);
        setSession(session);
        setLayer(layer);
        setStart(start);
        setEnd(end);
        setDetails(details);
        labels.put("appname", "nosqlbench");
    }

    public void setSession(String sessionName) {
        this.session = sessionName;
        this.labels.put("session", sessionName);
    }

    public void setStart(long intervalStart) {
        this.start = intervalStart;
        this.labels.put("span", getSpan().toString());
    }

    public void setEnd(long intervalEnd) {
        this.end = intervalEnd;
        this.labels.put("span", getSpan().toString());
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
        this.labels.put("layer", layer.toString());
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    @Override
    public String getSession() {
        return session;
    }

    @Override
    public long getStart() {
        return start;
    }

    @Override
    public long getEnd() {
        return end;
    }

    @Override
    public Layer getLayer() {
        return this.layer;
    }

    @Override
    public Map<String, String> getLabels() {
//        if (!labels.containsKey("span")) {
//            labels.put("span",getSpan().toString());
//        }
        return labels;
    }

    @Override
    public Map<String, String> getDetails() {
        return details;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("session: ").append(getSession()).append("\n");

        ZonedDateTime startTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(getStart()), zoneid);
        ZonedDateTime endTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(getStart()), zoneid);


        sb.append("[").append(startTime);
        if (getStart() != getEnd()) {
            sb.append(" - ").append(endTime);
        }
        sb.append("]\n");

        sb.append("span:").append(getSpan()).append("\n");
        sb.append("details:\n");
        formatMap(sb, getDetails());
        sb.append("labels:\n");
        formatMap(sb, getLabels());
        return sb.toString();
    }

    private void formatMap(StringBuilder sb, Map<String, String> details) {
        details.forEach((k, v) -> {
            sb.append(" ").append(k).append(": ");
            if (v.contains("\n")) {
                sb.append("\n");

                String[] lines = v.split("\n+");
                for (String line : lines) {
                    sb.append("  " + line + "\n");
                }
//                Arrays.stream(lines).sequential().map(s -> "  "+s+"\n").forEach(sb::append);
            } else {
                sb.append(v).append("\n");
            }
        });
    }

    public Annotation asReadOnly() {
        return this;
    }

    public Span getSpan() {
        return (getStart() == getEnd()) ? Span.instant : Span.interval;
    }

    public String asJson() {
        String inlineForm = gson.toJson(this);
        return inlineForm;
    }
}
