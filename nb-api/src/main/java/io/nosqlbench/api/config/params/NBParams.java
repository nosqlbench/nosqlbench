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

package io.nosqlbench.api.config.params;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <H2>NB Params</H2>
 * <p>NBParams is the main entry point into accessing parameters in a type-safe way.
 * It provides a reader interface which normalizes all access patterns for reading
 * configuration parameters from a variety of sources.</p>
 * <br/>
 * <p>NBParams is not a general purpose data interface. It is a <em>named</em> parameter reading interface.
 * As such, all data which is presented for reading must be named at every level.
 * This means that index access such as '[3]' that you might see in other access
 * vernaculars is <em>NOT</em> supported.</p>
 * <br/>
 * <p>However, multiplicity is allowed at the API level in order to support reading
 * zero or more of something when the number of provided elements is intended to
 * be configurable. In this usage, direct indexing is not intended nor allowed,
 * but order is preserved. This means that if there are any dependency relationships
 * within multiple elements at the same level, the developer can rely on them
 * being provided in the order specified by the user or underlying configuration source.</p>
 * <br/>
 * <p>When configuration elements are named within the element definition, regardless
 * of the source, these names can be taken as naming structure. To enable this, simply
 * provide a name property on the element.</p>
 * <hr/>
 *
 * <H2>Element Naming</H2>
 *
 * <p>If an element contains a property named <i>name</i>, then the value of this property
 * is taken as the name of the element. This is useful in certain contexts when you
 * need to define a name at the source of a configuration element but expose it
 * to readers. This means that an element can be positioned within a hierarchic structure
 * simply by naming it appropriately.</p>
 * <hr/>
 *
 * <H2>Element Views</H2>
 *
 * <p>This API allows a developer to choose the structural model imposed on
 * configuration data. Specifically, you must choose whether to consume the
 * parameter data as a set of properties of one element instance, or as as set
 * of elements, each with their own properties.</p>
 *
 * <pre>{@code
 *  view                           as single                      as multiple
 *  source is single element       <param name>                   ERROR
 *  source is multiple elements    <element name>.<param name>    iterable of elements
 * }</pre>
 *
 * <br/>
 *
 * This decision is made by which access method below is used. It is key that the
 * format of the provided data and the interpretation are in alignment. To ensure that
 * the consumer side of this API uses the configuration data appropriately, make
 * sure that users are well informed on valid formats. Basically, write good
 * docs and provide good examples in them.
 * <hr/>
 *
 * <H2>Single-Element View</H2>
 *
 * <p>The <i>one element access</i> interface is meant to provide basic support for
 * parameterizing a single entity. The names of the parameters surfaced at the
 * top level should map directly to the names of properties as provided by the
 * underlying data source. This is irrespective of whatever other structure may
 * be contained within such properties. The key distinction is that the top level
 * names of the configuration object are available under the same top-level names
 * within the one element interface.</p>
 * <br/>
 *
 * <H2>Avoiding Ambiguity</H2>
 * <p>As data sources may provide either one or many style results, and the consumption
 * patterns of these are different, a distinction must be made by each data source
 * that is faithful to users' expectations. In other words, it must be clear to users
 * when a single entity is being configured and when multiple entities are being configured.
 * Take for example, configuration of a single user's preferences. In this case, you would have
 * a set of preferences each with a name. The user providing the configuration values <EM>AND</EM>
 * the developer who writes code to consume these values must both agree that they are configuring
 * <EM>A set of named preference values for a single user entity.</EM> However, if you were intending
 * to read the configuration values for a set of users, each with their own set of configuration values,
 * then as before, it must be clear to the user providing the values as well as the developer writing
 * code to consume these values that they are communicating <em>A set of user preferences objects,
 * each consisting of a set of named user preference values.</em>. If the programmer were to read a
 * single preference object as a set of preferences for many users, a logical error would occur, at least.
 * Likewise, reading a named set of preferences objects as a single preference object would cause a logical
 * error.</p>
 *
 * <br/>
 * <p>When explicitly reading a configuration source as a single element, the underlying data source must provide
 * exactly one element <EM>OR</EM> provide a series of elements of which some contain
 * <i>name</i> properties. Non-distinct names are allowed, although the last element
 * for a given name will be the only one visible to readers. It is an error for the
 * underlying data source in this mode to be null, empty, or otherwise provide zero
 * elements. When multiple elements are provided, It is also an error if
 * none of them has a name property. Otherwise, those with no name property are
 * silently ignored and the ones with a name property are exposed.</p>
 * <hr/>
 * <H2>Element-List View</H2>
 * <p>When accessing <i>some elements</i>, any number of elements may be provided, even zero.</p>
 * <hr/>
 * <H2>Naming</H2>
 * <p>A parameter can be read from a reader by simple name or by a hierarchic name.
 * hierarchic names are simply simple names concatenated by a dot '.'.</p>
 */
public class NBParams {

    public static List<Element> some(Object source) {
        return DataSources
            .elements(source)
            .stream()
            .map(ElementImpl::new)
            .collect(Collectors.toList());
    }

    public static Element one(Object source) {
        return one(null, source);
    }

    public static Element one(String givenName, Object source) {
        List<ElementData> some = DataSources.elements(givenName,source);
        if (some.size() == 0) {
            throw new RuntimeException("One param object expected, but none found in '" + source + "'");
        }
        if (some.size() > 1) {
            Map<String, ElementData> data = new LinkedHashMap<>();
            for (ElementData elementData : some) {
                String name = elementData.getName();
                if (name != null && !name.isBlank()) {
                    data.put(name, elementData);
                }
            }
            if (data.isEmpty()) {
                throw new RuntimeException("multiple elements found, but none contained a name for flattening to a map.");
            }
            return new ElementImpl(new MapBackedElement(givenName,data));
        }
        return new ElementImpl(some.get(0));
    }


}
