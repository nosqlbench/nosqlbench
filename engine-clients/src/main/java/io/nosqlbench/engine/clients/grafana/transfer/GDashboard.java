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

package io.nosqlbench.engine.clients.grafana.transfer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GDashboard {
    Map<String, Object> annotations = new HashMap<>();
    String description;
    boolean editable;
    long graphToolTip;
    long id;
    long iteration;
    List<Object> links;
    List<GPanelDef> panels;
    String refresh;
    long schemaVersion;
    String style;
    List<Object> tags;
    GTemplating templating;
    Time time;
    Map<String, List<String>> timepicker;
    String timezone;
    String title;
    String uid;
    long version;

    public Map<String, Object> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, Object> annotations) {
        this.annotations = annotations;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public long getGraphToolTip() {
        return graphToolTip;
    }

    public void setGraphToolTip(long graphToolTip) {
        this.graphToolTip = graphToolTip;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIteration() {
        return iteration;
    }

    public void setIteration(long iteration) {
        this.iteration = iteration;
    }

    public List<Object> getLinks() {
        return links;
    }

    public void setLinks(List<Object> links) {
        this.links = links;
    }

    public List<GPanelDef> getPanels() {
        return panels;
    }

    public void setPanels(List<GPanelDef> panels) {
        this.panels = panels;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }

    public long getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(long schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public List<Object> getTags() {
        return tags;
    }

    public void setTags(List<Object> tags) {
        this.tags = tags;
    }

    public GTemplating getTemplating() {
        return templating;
    }

    public void setTemplating(GTemplating templating) {
        this.templating = templating;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public Map<String, List<String>> getTimepicker() {
        return timepicker;
    }

    public void setTimepicker(Map<String, List<String>> timepicker) {
        this.timepicker = timepicker;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public static GDashboard fromFile(String path) {
//        URL resource = GDashboard.class.getClassLoader().getResource(path);


        String json = null;

        try {
            URL url = GDashboard.class.getClassLoader().getResource(path);
            if (url == null) {
                throw new RuntimeException("path not found:" + path);
            }
            Path found = Paths.get(url.toURI());
            byte[] bytes = Files.readAllBytes(found);
            json = new String(bytes, StandardCharsets.UTF_8);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            GDashboard db = gson.fromJson(json, GDashboard.class);
            return db;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Time getTime() {
        return time;
    }

    public static class Time {
        String from;
        String to;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }
}
