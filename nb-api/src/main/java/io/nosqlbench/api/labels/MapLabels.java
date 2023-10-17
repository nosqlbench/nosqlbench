/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.api.labels;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public class MapLabels implements NBLabels {

//    private final static Logger logger = LogManager.getLogger(MapLabels.class);
    private final Map<String,String> labels;

    public MapLabels(final Map<String, String> labels) {
        verifyValidNamesAndValues(labels);
//        verifyValidValues(labels);
        this.labels = Collections.unmodifiableMap(labels);
    }


    public MapLabels(final Map<String,String> parentLabels, final Map<String,String> childLabels) {
        final Map<String, String> combined = new LinkedHashMap<>(parentLabels);
        childLabels.forEach((k,v) -> {
            if (combined.containsKey(k))
                throw new RuntimeException("Can't overlap label keys (for instance " + k + ") between parent and child elements. parent:" + parentLabels + ", child:" + childLabels);
            combined.put(k,v);
        });
        verifyValidNamesAndValues(combined);
//        verifyValidValues(combined);
        labels=Collections.unmodifiableMap(combined);
    }

    private final Pattern validNamesPattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*");
    private void verifyValidNamesAndValues(Map<String, String> labels) {
        labels.forEach((label,value) -> {
            if (!validNamesPattern.matcher(label).matches()) {
                throw new RuntimeException("Invalid label name '" + label + "', only a-z,A-Z,_ are allowed as the initial character, and a-z,A-Z,0-9,_ are allowed after.");
            }
//            if (!validNamesPattern.matcher(value).matches()) {
//                throw new RuntimeException("Invalid label value '" + value + "', only a-z,A-Z,_ are allowed as the initial character, and a-z,A-Z,0-9,_ are allowed after.");
//            }
        });
    }

    private void verifyValidValues(Map<String, String> labels) {
        for (String value : labels.values()) {
            if (!validNamesPattern.matcher(value).matches()) {
                throw new RuntimeException("Invalid label value '" + value + "', only a-z,A-Z,_ are allowed as the initial character, and a-z,A-Z,0-9,_ are allowed after.");
            }
        }
    }



    @Override
    public String linearizeValues(final char delim, final String... included) {
        final StringBuilder sb = new StringBuilder();
        final List<String> includedNames = new ArrayList<>();
        if (0 < included.length) Collections.addAll(includedNames, included);
        else includedNames.addAll(this.labels.keySet());

        for (String includedName : includedNames) {
            final boolean optional= includedName.startsWith("[") && includedName.endsWith("]");
            includedName=optional?includedName.substring(1,includedName.length()-1):includedName;

            final String component = this.labels.get(includedName);
            if (null == component) {
                if (optional) continue;
                throw new RuntimeException("label component '" + includedName + "' was null.");
            }
            sb.append(component).append(delim);
        }
        sb.setLength(sb.length()-1);
        return sb.toString();
    }

    @Override
    public String linearize_bare(String... barewords) {
        StringBuilder sb = new StringBuilder();
        Set<String> keyset = new HashSet<>(labels.keySet());
        for (String bareword : barewords) {
            if (keyset.contains(bareword)) {
                keyset.remove(bareword);
                sb.append(labels.get(bareword)).append("__");
            }
        }
        if (!sb.isEmpty()) {
            sb.setLength(sb.length()-"__".length());
        }

        List<String> keys = new ArrayList<>(keyset);
        if (!keys.isEmpty()) {
            Collections.sort(keys);
            for (String key : keys) {
                sb.append("_").append(key).append("_").append(labels.get(key)).append("__");
            }
            sb.setLength(sb.length()-"__".length());
        }
        return sb.toString();
    }

    @Override
    public String linearize(String bareName, String... included) {
        final StringBuilder sb = new StringBuilder();

        final List<String> includedNames = new ArrayList<>();
        if (0 < included.length) Collections.addAll(includedNames, included);
        else includedNames.addAll(this.labels.keySet());
        String rawName = null;
        if (null != bareName) {
            rawName = this.labels.get(bareName);
            includedNames.remove(bareName);
            if (null == rawName) throw new RuntimeException("Unable to get value for key '" + bareName + '\'');
            sb.append(rawName);
        }
        if (!includedNames.isEmpty()) {
            sb.append('{');
            for (final String includedName : includedNames) {
                final String includedValue = this.labels.get(includedName);
                Objects.requireNonNull(includedValue);
                sb.append(includedName)
                    .append("=\"")
                    .append(includedValue)
                    .append('"')
                    .append(',');
            }
            sb.setLength(sb.length()-",".length());
            sb.append('}');
        }

        return sb.toString();
    }

    @Override
    public String linearizeAsMetrics() {
        if (labels.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        ArrayList<String> keys = new ArrayList<>(this.labels.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            sb.append(key).append("=\"").append(labels.get(key)).append("\",");
        }
        sb.setLength(sb.length()-",".length());
        sb.append("}");
        return sb.toString();

    }


    @Override
    public String linearizeAsKvString() {
        if (labels.isEmpty()) {
            return "EMPTY";
        }
        StringBuilder sb = new StringBuilder("");
        ArrayList<String> keys = new ArrayList<>(this.labels.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            sb.append(key).append("=").append(labels.get(key)).append(",");
        }
        sb.setLength(sb.length()-",".length());
        return sb.toString();

    }

    public static String sanitize(String word) {
        String sanitized = word;
        sanitized = sanitized.replaceAll("\\.", "__");
        sanitized = sanitized.replaceAll("-", "_");
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9_]+", "");

//        if (!word.equals(sanitized)) {
//            logger.warn("The identifier or value '" + word + "' was sanitized to '" + sanitized + "' to be compatible with monitoring systems. You should probably change this to make diagnostics easier.");
//        }
        return sanitized;
    }


    @Override
    public MapLabels and(final Object... labelsAndValues) {
        final Map<String,String> childLabels = getStringStringMap(labelsAndValues);
        return new MapLabels(labels,childLabels);
    }

    @Override
    public MapLabels and(NBLabels labels) {
        return new MapLabels(this.labels,labels.asMap());
    }
    @Override
    public NBLabels modifyName(final String nameToModify, final Function<String, String> transform) {
        if (!this.labels.containsKey(nameToModify))
            throw new RuntimeException("Missing name in labels for transform: '" + nameToModify + '\'');
        final LinkedHashMap<String, String> newLabels = new LinkedHashMap<>(this.labels);
        final String removedValue = newLabels.remove(nameToModify);
        final String newName = transform.apply(nameToModify);
        newLabels.put(newName,removedValue);
        return new MapLabels(newLabels);
    }

    @Override
    public NBLabels modifyValue(final String labelName, final Function<String, String> transform) {
        if(!this.labels.containsKey(labelName))
            throw new RuntimeException("Unable to find label name '" + labelName + "' for value transform.");
        final LinkedHashMap<String, String> newMap = new LinkedHashMap<>(this.labels);
        final String value = newMap.remove(labelName);
        if (null == value) throw new RuntimeException("The value for named label '" + labelName + "' is null.");
        newMap.put(labelName,transform.apply(value));
        return NBLabels.forMap(newMap);
    }

    public String toString() {
        if (labels.size()==0) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        labels.forEach((k,v) -> {
            sb.append(k).append(":\\\"").append(v).append("\\\"").append(",");
        });
        sb.setLength(sb.length()-",".length());
        sb.append("}");

        return sb.toString();
    }

    @Override
    public String valueOf(final String name) {
        if (!this.labels.containsKey(name))
            throw new RuntimeException("The specified key does not exist: '" + name + '\'');
        final String only = labels.get(name);
        if (null == only) throw new RuntimeException("The specified value is null for key '" + name + '\'');
        return only;
    }

    @Override
    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(labels);
    }

    @Override
    public NBLabels and(final Map<String, String> moreLabels) {
        return new MapLabels(this.labels, moreLabels);
    }

    private String[] concat(String[] a, String[] b) {
        String[] c = new String[a.length+b.length];
        System.arraycopy(a,0,c,0,a.length);
        System.arraycopy(b,0,c,a.length,b.length);
        return c;
    }

    private static String[] getNamesArray(final Object... labelsAndValues) {
        String[] keys = new String[labelsAndValues.length>>1];
        for (int i = 0; i < keys.length; i++) {
            keys[i]=labelsAndValues[i<<1].toString();
        }
        return keys;
    }
    @NotNull
    private static Map<String, String> getStringStringMap(Object[] labelsAndValues) {
        if (0 != (labelsAndValues.length % 2))
            throw new RuntimeException("Must provide even number of keys and values: " + Arrays.toString(labelsAndValues));
        final Map<String, String> childLabels = new LinkedHashMap<>();
        for (int i = 0; i < labelsAndValues.length; i+=2) childLabels.put(labelsAndValues[i].toString(), labelsAndValues[i + 1].toString());
        return childLabels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MapLabels mapLabels = (MapLabels) o;

        return Objects.equals(labels, mapLabels.labels);
    }

    @Override
    public int hashCode() {
        return labels != null ? labels.hashCode() : 0;
    }


    /**
     * Take the intersection of the two label sets, considering both key
     * and value for each label entry. If both have the same label name
     * but different values for it, then that label is not considered
     * common and it is not retained in the intersection.
     * @param otherLabels The label set to intersect
     */
    @Override
    public NBLabels intersection(NBLabels otherLabels) {
        Map<String, String> other = otherLabels.asMap();
        Map<String,String> common = new LinkedHashMap<>();
        asMap().forEach((k,v) -> {
            if (other.containsKey(k) && other.get(k).equals(v)) {
                common.put(k,v);
            }
        });
        return NBLabels.forMap(common);
    }

    /**
     * Subtract all matching labels from the other label set from this one,
     * considering label names and values. If the other label set contains
     * the same name but a different value, then it is not considered a
     * match and thus not removed from the labels of this element.
     * @param otherLabels Labels to remove, where key and value matches
     * @return The same, or a smaller set of labels for this element
     */
    @Override
    public NBLabels difference(NBLabels otherLabels) {
        Map<String, String> other = otherLabels.asMap();
        NBLabels difference = NBLabels.forKV();
        for (String key : labels.keySet()) {
            if (!other.containsKey(key) || !other.get(key).equals(labels.get(key))) {
                difference = difference.and(key,labels.get(key));
            }
        }
        return difference;
    }

}
