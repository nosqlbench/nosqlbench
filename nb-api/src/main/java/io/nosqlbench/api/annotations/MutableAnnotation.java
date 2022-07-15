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

package io.nosqlbench.api.annotations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MutableAnnotation implements Annotation {

    private final static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private String session = "SESSION_UNNAMED";
    private final ZoneId GMT = ZoneId.of("GMT");

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

        sb.append("[");
        ZonedDateTime zonedStartTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(getStart()), zoneid);
        sb.append(zonedStartTime.format(DateTimeFormatter.ISO_INSTANT));
        if (getStart() != getEnd()) {
            ZonedDateTime zonedEndTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(getEnd()), zoneid);
            sb.append(" - ").append(zonedEndTime.format(DateTimeFormatter.ISO_INSTANT));
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
