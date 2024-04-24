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

package io.nosqlbench.docsys.api;

/**
 * Any class which is annotated with <pre>{@code @Service(WebServiceObject.class)}</pre>
 * will be found and loaded as a service.
 *
 * For the methods used to configure these objects, consult the
 * references below:
 *
 * @see <A href="https://eclipse-ee4j.github.io/jersey.github
 * .io/documentation/latest/jaxrs-resources.html#d0e2040">Jersey jax-rs
 * resources documentation</A>
 *
 * @see <A href="https://repo1.maven.org/maven2/org/glassfish/jersey/jersey-documentation/2.5
 * .1/jersey-documentation-2.5.1-user-guide.pdf">Jersey 2.5.1 user guide</a>
 *
 * @see <A href="https://github.com/jax-rs/spec/blob/master/spec
 * .pdf">JAX-RS: Java™ API for RESTful Web Services Version 2.1
 * Proposed Final Draft June 9, 2017</A>
 **
 * @see <A href="https://jax-rs.github.io/apidocs/2.1/">Jax-RS API Docs</A>
 */
public interface WebServiceObject {
}
