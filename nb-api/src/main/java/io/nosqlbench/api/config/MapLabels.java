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

package io.nosqlbench.api.config;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public class MapLabels implements NBLabels {
    private final Map<String,String> labels;
    private String[] instanceFields = new String[0];

    public MapLabels(final Map<String, String> labels, String... instanceFields) {
        verifyValidNamesAndValues(labels);
//        verifyValidValues(labels);
        this.labels = Collections.unmodifiableMap(labels);
        this.instanceFields = instanceFields;
    }


    public MapLabels(final Map<String,String> parentLabels, final Map<String,String> childLabels, String... instanceFields) {
        final Map<String, String> combined = new LinkedHashMap<>(parentLabels);
        childLabels.forEach((k,v) -> {
            if (combined.containsKey(k))
                throw new RuntimeException("Can't overlap label keys (for instance " + k + ") between parent and child elements. parent:" + parentLabels + ", child:" + childLabels);
            combined.put(k,v);
        });
        verifyValidNamesAndValues(combined);
//        verifyValidValues(combined);
        this.instanceFields = instanceFields;
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
    public String linearize(String bareName, String... included) {
        final StringBuilder sb = new StringBuilder();

        final List<String> includedNames = new ArrayList<>();
        if (0 < included.length) Collections.addAll(includedNames, included);
        else includedNames.addAll(this.labels.keySet());
        String rawName = null;
        if (null != bareName) {
            rawName = this.labels.get(bareName);
            if (null == rawName) throw new RuntimeException("Unable to get value for key '" + bareName + '\'');
            sb.append(rawName);
        }
        if (0 < includedNames.size()) {
            sb.append('{');
            for (final String includedName : includedNames) {
                if (includedName.equals(bareName)) continue;
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
    public MapLabels andTypes(final Object... labelsAndValues) {
        final Map<String,String> childLabels = getStringStringMap(labelsAndValues);
        return new MapLabels(labels,childLabels);
    }

    @Override
    public MapLabels and(NBLabels labels) {
        return new MapLabels(this.labels,labels.asMap(), concat(this.instanceFields,labels.getInstanceFields()));
    }


    @Override
    public MapLabels andInstances(final Object... labelsAndValues) {
        final Map<String,String> childLabels = getStringStringMap(labelsAndValues);
        String[] childInstanceFields = getNamesArray(labelsAndValues);
        return new MapLabels(this.labels,childLabels,concat(this.instanceFields,getNamesArray(labelsAndValues)));
    }

    @Override
    public MapLabels andInstances(Map<String, String> instanceLabelsAndValues) {
        return new MapLabels(this.labels,instanceLabelsAndValues,instanceLabelsAndValues.keySet().toArray(new String[0]));
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
    public NBLabels onlyTypes() {
        Map<String,String> typesOnlyMap = new LinkedHashMap<>(this.labels);
        for (String instanceField : this.instanceFields) {
            typesOnlyMap.remove(instanceField);
        }
        return new MapLabels(typesOnlyMap);
    }

    @Override
    public NBLabels onlyInstances() {
        Map<String,String> instancesOnlyMap = new LinkedHashMap<>();
        for (String instanceField : this.instanceFields) {
            instancesOnlyMap.put(instanceField,this.labels.get(instanceField));
        }
        return new MapLabels(instancesOnlyMap);
    }

    @Override
    public String[] getInstanceFields() {
        return instanceFields;
    }

    @Override
    public NBLabels andTypes(final Map<String, String> moreLabels) {
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
}
