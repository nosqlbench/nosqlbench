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

package io.nosqlbench.engine.clients.grafana;

import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * Add one tag at a time, in either "tag" or "tag:value" form.
     */
    public static By tag(String tag) {
        return new By("tag", tag);
    }

    public static By id(int id) {
        return new By("id", id);
    }

    public static String urlEncoded(By... bys) {
        List<String> tags = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (By by : bys) {
            if (by.key.equals("tag")) {
                tags.addAll(Arrays.asList(by.value.toString().split(",")));
            } else {
                sb.append(by.key).append("=").append(by.value);
                sb.append("&");
            }
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
