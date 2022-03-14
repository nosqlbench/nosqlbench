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

import java.util.*;

public class GAnnotation {

    private Integer id;
    private Integer alertId;
    private Integer dashboardId;
    private Integer panelId;
    private Integer userId;
    private String userName;
    private String newState;
    private String prevState;
    private Long time;
    private Long timeEnd;
    private String text;
    private String metric;
    private String type;
    private final List<String> tags = new ArrayList<>();
    //    private Map<String, String> tags = new LinkedHashMap<>();
    private Object data;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAlertId() {
        return alertId;
    }

    public void setAlertId(Integer alertId) {
        this.alertId = alertId;
    }

    public Integer getDashboardId() {
        return dashboardId;
    }

    public void setDashboardId(Integer dashboardId) {
        this.dashboardId = dashboardId;
    }

    public Integer getPanelId() {
        return panelId;
    }

    public void setPanelId(Integer panelId) {
        this.panelId = panelId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public String getPrevState() {
        return prevState;
    }

    public void setPrevState(String prevState) {
        this.prevState = prevState;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(Long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTags() {
        return tags;
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public void setTags(List<String> tags) {
        tags.forEach(this::addTag);
    }

    public void setTags(Map<String, String> tags) {
        tags.forEach((k, v) -> {
            this.addTag(k + ":" + v);
        });
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "id=" + id +
                ", alertId=" + alertId +
                ", dashboardId=" + dashboardId +
                ", panelId=" + panelId +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", newState='" + newState + '\'' +
                ", prevState='" + prevState + '\'' +
                ", time=" + time +
                ", timeEnd=" + timeEnd +
                ", text='" + text + '\'' +
                ", metric='" + metric + '\'' +
                ", type='" + type + '\'' +
                ", tags=" + tags +
                ", data=" + data +
                '}';
    }

    public LinkedHashMap<String, String> getTagMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (String tag : this.getTags()) {
            String[] split = tag.split(":", 2);
            map.put(split[0], (split.length == 2 ? split[1] : null));
        }
        return map;
    }
}
