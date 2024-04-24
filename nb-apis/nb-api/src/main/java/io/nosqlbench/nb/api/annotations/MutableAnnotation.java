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

package io.nosqlbench.nb.api.annotations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;

public class MutableAnnotation implements Annotation {

    private final static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    public static String session = "SESSION_UNNAMED";
    private final ZoneId GMT = ZoneId.of("GMT");

    private final LinkedList<Function<NBLabels,NBLabels>> labelfuncs = new LinkedList<>();

    @Expose
    private Layer layer;

    @Expose
    private long start = 0L;

    @Expose
    private long end = 0L;

    @Expose
    private Map<String, String> details = new LinkedHashMap<>();

    NBLabels labels = NBLabels.forKV();

    private final ZoneId zoneid = ZoneId.of("GMT");
    private NBLabeledElement element;

    public MutableAnnotation(
            TimeZone timezone,
            String session,
            Layer layer,
            long start,
            long end,
            NBLabeledElement element,
            LinkedHashMap<String, String> details) {
        setElement(element);
        setSession(session);
        setLayer(layer);
        setStartMillis(start);
        setEndMillis(end);
        setDetails(details);
    }

    private void setElement(NBLabeledElement element) {
        this.element = element;
        this.labels = element.getLabels();
    }

    public void setSession(String sessionName) {
        session = sessionName;
    }

    public void setStartMillis(long intervalStart) {
        this.start = intervalStart;
    }

    public void setEndMillis(long intervalEnd) {
        this.end = intervalEnd;
        this.details.put("duration",String.valueOf(getDurationMillis()));
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    @Override
    public long getStartMillis() {
        return start;
    }

    @Override
    public long getEndMillis() {
        return end;
    }

    @Override
    public Layer getLayer() {
        return this.layer;
    }

    @Override
    public NBLabels getLabels() {
        NBLabels labels= element.getLabels();
        for (Function<NBLabels, NBLabels> f : labelfuncs) {
            labels = f.apply(labels);
        }
        return labels;
    }

    @Override
    public void applyLabelFunction(Function<NBLabels, NBLabels> labelfunc) {
        labelfuncs.add(labelfunc);
    }

    @Override
    public Map<String, String> getDetails() {
        return details;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        ZonedDateTime startTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(getStartMillis()), zoneid);
        ZonedDateTime endTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(getStartMillis()), zoneid);

        sb.append("[");
        ZonedDateTime zonedStartTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(getStartMillis()), zoneid);
        sb.append(zonedStartTime.format(DateTimeFormatter.ISO_INSTANT));
        if (getStartMillis() != getEndMillis()) {
            ZonedDateTime zonedEndTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(getEndMillis()), zoneid);
            sb.append(" - ").append(zonedEndTime.format(DateTimeFormatter.ISO_INSTANT));
        }
        sb.append("]\n");

        sb.append("span:").append(getTemporal()).append("\n");
        sb.append("details:\n");
        formatMap(sb, getDetails());
        sb.append("labels:\n");
        formatMap(sb, getLabels().asMap());
        return sb.toString();
    }

    private void formatMap(StringBuilder sb, Map<String, String> details) {
        details.forEach((k, v) -> {
            sb.append(" ").append(k).append(":");
            if (v.contains("\n")) {
                sb.append("\n");

                String[] lines = v.split("\n+");
                for (String line : lines) {
                    sb.append("  " + line + "\n");
                }
//                Arrays.stream(lines).sequential().map(s -> "  "+s+"\n").forEach(sb::append);
            } else {
                sb.append(" ").append(v).append("\n");
            }
        });
    }

    public Annotation asReadOnly() {
        return this;
    }

    public Temporal getTemporal() {
        return (getStartMillis() == getEndMillis()) ? Temporal.instant : Temporal.interval;
    }

    public String asJson() {
        String inlineForm = gson.toJson(this);
        return inlineForm;
    }
}
