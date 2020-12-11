package io.nosqlbench.engine.clients.grafana.transfer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dashboard {
    Map<String, Object> annotations = new HashMap<>();
    String description;
    boolean editable;
    long graphToolTip;
    long id;
    long iteration;
    List<Object> links;
    List<PanelDef> panels;
    String refresh;
    long schemaVersion;
    String style;
    List<Object> tags;
    Map<String, Object> templating;
    Map<String, Object> time;
    Map<String, List<String>> timepicker;
    String timezone;
    String title;
    String uid;
    long version;

}
