package io.nosqlbench.engine.clients.grafana;

import java.util.ArrayList;
import java.util.List;

public class By {

    private final String key;
    private final Object value;

    public By(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    /**
     * epoch datetime in milliseconds
     */
    public static By from(long epoch) {
        return new By("from", epoch);
    }

    /**
     * epoch datetime in milliseconds
     */
    public static By to(long epoch) {
        return new By("to", epoch);
    }

    /**
     * number. Optional - default is 100. Max limit for results returned.
     */
    public static By limit(long limit) {
        return new By("limit", limit);
    }

    /**
     * Find annotations for a specified alert.
     */
    public static By alertId(String id) {
        return new By("alertId", id);
    }

    public static By panelId(String panelId) {
        return new By("panelId", panelId);
    }

    public static By userId(String userId) {
        return new By("userId", userId);
    }

    public static By typeAnnotation() {
        return new By("type", "annotation");
    }

    public static By typeAlert() {
        return new By("type", "alert");
    }

    public static By tags(String tag) {
        return new By("tags", tag);
    }

    public static By id(int id) {
        return new By("id", id);
    }

    public static String urlEncoded(By... bys) {
        List<String> tags = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (By by : bys) {
            if (by.key.equals("tags")) {
                tags.add(by.value.toString());
            }
            sb.append(by.key).append("=").append(by.value);
            sb.append("&");
        }
        for (String tag : tags) {
            sb.append("tags=").append(tag).append("&");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }


    public static String fields(By... by) {
        return urlEncoded(by);
    }
}
