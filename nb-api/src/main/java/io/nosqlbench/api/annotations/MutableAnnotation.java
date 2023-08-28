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

package io.nosqlbench.api.annotations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.config.NBLabels;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

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
    private Map<String, String> details = new LinkedHashMap<>();

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
        setStart(start);
        setEnd(end);
        setDetails(details);
    }

    private void setElement(NBLabeledElement element) {
        this.element = element;
    }

    public void setSession(String sessionName) {
        this.session = sessionName;
    }

    public void setStart(long intervalStart) {
        this.start = intervalStart;
    }

    public void setEnd(long intervalEnd) {
        this.end = intervalEnd;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
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
    public NBLabels getLabels() {
        return element.getLabels();
    }

    @Override
    public Map<String, String> getDetails() {
        return details;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

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
        return (getStart() == getEnd()) ? Temporal.instant : Temporal.interval;
    }

    public String asJson() {
        String inlineForm = gson.toJson(this);
        return inlineForm;
    }
}
