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

package io.nosqlbench.virtdata.core.bindings;

//

import io.nosqlbench.virtdata.core.templates.BindPoint;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * Maps a set of parameters on an associated object of type T to specifiers for data mappers.
 * Allows for easy construction of DataMapperBindings when in the proper thread scope.
 * <p>
 * The user is required to call @{link resolveBindings} when in the scope that the resulting
 * bindings will be used in.
 */
public class BindingsTemplate {

    private final static Logger logger = LogManager.getLogger(BindingsTemplate.class);
    private final Map<String, Object> fconfig;
    private final List<String> bindPointNames = new ArrayList<>();
    private final List<String> specifiers = new ArrayList<>();

    public BindingsTemplate(Map<String,Object> config, List<String> anchors, List<String> specs) {
        this.fconfig = config;
        if (anchors.size() != specs.size()) {
            throw new InvalidParameterException("Anchors and Specifiers must be matched pair-wise.");
        }
        for (int i = 0; i < anchors.size(); i++) {
            addFieldBinding(anchors.get(i), specs.get(i));
        }
    }

    public BindingsTemplate(Map<String,Object> config, List<BindPoint> bindpoints) {
        this.fconfig = config;
        addFieldBindings(bindpoints);
    }

    public BindingsTemplate(List<BindPoint> bindPoints) {
        this.fconfig = Map.of();
        addFieldBindings(bindPoints);
    }

    public BindingsTemplate(Map<String,Object> config) {
        this.fconfig = config;
    }

    public BindingsTemplate() {
        this.fconfig = Map.of();
    }

    public void addFieldBindings(List<BindPoint> bindPoints) {
        for (BindPoint bindPoint : bindPoints) {
            addFieldBinding(bindPoint.getAnchor(), bindPoint.getBindspec());
        }
    }

    /**
     * Add a named binding specifier to the template
     *
     * @param bindPointName the name associated with the binding specifier
     * @param genSpec       the binding specifier
     */
    public void addFieldBinding(String bindPointName, String genSpec) {
        this.bindPointNames.add(bindPointName);
        this.specifiers.add(genSpec);
    }

    /**
     * Add multiple named bindings to the template
     *
     * @param bindPairs A map of named binding specifiers
     */
    public BindingsTemplate addFieldBindings(Map<String, String> bindPairs) {
        for (Map.Entry<String, String> e : bindPairs.entrySet()) {
            this.bindPointNames.add(e.getKey());
            this.specifiers.add(e.getValue());
        }
        return this;
    }

    public String getDiagnostics() {
        StringBuilder diaglog = new StringBuilder();
        for (String specifier : specifiers) {
            diaglog.append("for ").append(specifier).append(":");

            ResolverDiagnostics mapperDiagnostics = VirtData.getMapperDiagnostics(specifier);
            String diagnostics = mapperDiagnostics.toString();
            diaglog.append(diagnostics);
            if (mapperDiagnostics.getResolvedFunction().isPresent()) {
                diaglog.append("☑ RESOLVED:")
                    .append(mapperDiagnostics.getResolvedFunction().get()).append("\n");
            } else {
                diaglog.append("☐ UNRESOLVED\n");
            }
        }
        return diaglog.toString();
    }

    /**
     * Use the data mapping library and the specifier to create instances of data mapping functions.
     * If you need thread-aware mapping, be sure to call this in the proper thread. Each time this method
     * is called, it creates a new instance.
     *
     * @return A set of bindings that can be used to yield mapped data values later.
     */
    public Bindings resolveBindings() {
        List<DataMapper<?>> dataMappers = new ArrayList<>();
        for (String specifier : specifiers) {
            Optional<DataMapper<Object>> optionalDataMapper = VirtData.getOptionalMapper(specifier,fconfig);
            if (optionalDataMapper.isPresent()) {
                dataMappers.add(optionalDataMapper.get());
            } else {
                logAvailableDataMappers();
                throw new RuntimeException(
                    "data mapper binding was unsuccessful for "
                        + ", spec:" + specifier
                        + ", see log for known data mapper names.");
            }
        }
        return new Bindings(this, dataMappers);
    }

    private void logAvailableDataMappers() {
        VirtDataDocs.getAllNames().forEach(gn -> logger.info(() -> "DATAMAPPER " + gn));
    }

    public List<String> getBindPointNames() {
        return this.bindPointNames;
    }

    public List<String> getDataMapperSpecs() {
        return this.specifiers;
    }

    @Override
    public String toString() {
        String delim = "";
        StringBuilder sb = new StringBuilder(BindingsTemplate.class.getSimpleName()).append(":");
        for (int i = 0; i < bindPointNames.size(); i++) {
            sb.append(delim);
            sb.append("'").append(bindPointNames.get(i)).append("'");
            sb.append("=>");
            sb.append("\"").append(specifiers.get(i)).append("\"");
            delim = ", ";
        }
        return sb.toString();
    }

    public String toString(Object[] values) {
        String delim = "";
        StringBuilder sb = new StringBuilder(BindingsTemplate.class.getSimpleName()).append(":");
        for (int i = 0; i < bindPointNames.size(); i++) {
            sb.append(delim);
            sb.append("'").append(bindPointNames.get(i)).append("'");
            sb.append("=>");
            sb.append("\"").append(specifiers.get(i)).append("\"");
            sb.append("=>[");
            sb.append(values[i]);
            sb.append("](");
            sb.append((null != values[i]) ? values[i].getClass().getSimpleName() : "NULL");
            sb.append(")");
            delim = ", ";
        }
        return sb.toString();
    }

    public Map<String, String> getMap() {
        LinkedHashMap<String, String> bindmap = new LinkedHashMap<>();
        for (int i = 0; i < this.specifiers.size(); i++) {
            bindmap.put(this.bindPointNames.get(i),this.specifiers.get(i));
        }
        return bindmap;
    }
}
