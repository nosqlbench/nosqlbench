package io.nosqlbench.engine.clients.grafana.transfer;

import java.util.List;

public class GTemplate {
    String allValue;
    String name;
    GCurrentValue current;
    String datasource;
    String definition;
    Object error;
    long hide;
    boolean includeAll;
    String label;
    boolean multi;
    List<Object> options;
    String query;
    long refresh;
    String regex;
    boolean skipUrlSync;
    long sort;
    String tagValuesQuery;
    List<String> tags;
    String tagsQuery;
    String type;
    boolean useTags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAllValue() {
        return allValue;
    }

    public void setAllValue(String allValue) {
        this.allValue = allValue;
    }

    public GCurrentValue getCurrent() {
        return current;
    }

    public void setCurrent(GCurrentValue current) {
        this.current = current;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public long getHide() {
        return hide;
    }

    public void setHide(long hide) {
        this.hide = hide;
    }

    public boolean isIncludeAll() {
        return includeAll;
    }

    public void setIncludeAll(boolean includeAll) {
        this.includeAll = includeAll;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isMulti() {
        return multi;
    }

    public void setMulti(boolean multi) {
        this.multi = multi;
    }

    public List<Object> getOptions() {
        return options;
    }

    public void setOptions(List<Object> options) {
        this.options = options;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public long getRefresh() {
        return refresh;
    }

    public void setRefresh(long refresh) {
        this.refresh = refresh;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public boolean isSkipUrlSync() {
        return skipUrlSync;
    }

    public void setSkipUrlSync(boolean skipUrlSync) {
        this.skipUrlSync = skipUrlSync;
    }

    public long getSort() {
        return sort;
    }

    public void setSort(long sort) {
        this.sort = sort;
    }

    public String getTagValuesQuery() {
        return tagValuesQuery;
    }

    public void setTagValuesQuery(String tagValuesQuery) {
        this.tagValuesQuery = tagValuesQuery;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getTagsQuery() {
        return tagsQuery;
    }

    public void setTagsQuery(String tagsQuery) {
        this.tagsQuery = tagsQuery;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isUseTags() {
        return useTags;
    }

    public void setUseTags(boolean useTags) {
        this.useTags = useTags;
    }

    public static class GCurrentValue {
        boolean selected;
        Object text;
        Object value;

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public Object getText() {
            return text;
        }

        public void setText(Object text) {
            this.text = text;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public List<String> getValues() {
            if (value instanceof String) {
                return List.of((String) value);
            } else if (value instanceof List) {
                return (List<String>) value;
            } else {
                throw new RuntimeException("Unrecognized form of value:" + value.getClass().getSimpleName());
            }

        }

        public List<String> getTexts() {
            if (text instanceof String) {
                return List.of((String) text);
            } else if (text instanceof List) {
                return (List<String>) text;
            } else {
                throw new RuntimeException("Unrecognized form of text:" + text.getClass().getSimpleName());
            }
        }
    }
}
