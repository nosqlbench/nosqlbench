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

/**
 * <P>The component types in this package create a basic structural and lifecycle
 * pattern that NoSQLBench runtimes should build upon. This allows leverage of
 * the base component types to achieve:
 * <UL>
 * <LI>consistent runtime behavior between modules</LI>
 * <LI>modular composition of runtime modules in different configurations</LI>
 * <LI>a uniform configuration interface across all components</LI>
 * <LI>exposition of foundational runtime structure to users</LI>
 * <LI>consistent layering of components and their lifetimes at different runtime scopes</LI>
 * <LI>a naming and metadata based view which allows for components to be managed in a tangible way</LI>
 * <LI>a consistent manifest and registry interface for attached elements, like metrics</LI>
 * </UL>
 * </P>
 * <hr/>
 *
 * <H2>Components Defined</H2>
 * <P>There can be different layers or types of components, but there is only one component hierarchy in each
 * NoSQLBench process. Basically the Session is a component. The Scenario is a component. Each activity within a
 * scenario is a component. Whether something is meaningful as a component depends on whether the management facilities
 * provided by the component API make using, interacting with, or understanding that element better for users. However
 * there is a limit to how fine-grained the component hierarchy should be allowed to get. This is because maintaining
 * the component structure at runtime incurs a cost, and most feature of the component types are related to assemblage
 * of fixtures in the runtime which are configured and initialized before steady state processing begins. For example,
 * it makes sense to wire an activity as component, but not an operation, since an operation is ephemeral and
 * short-lived. Apart from these trade-offs, make a layer a component layer if it makes sense for the user and/or the
 * developer, as consolidating the logic into the component layer is beneficial to both.</P>
 *
 * <H2>Adoption Strategy</H2>
 * <P>Consolidating existing logic to use the component types will be an incremental process. The base contract type
 * {@link io.nosqlbench.nb.api.components.NBComponent} establishes the contract for any conforming types. As contract facets
 * are added to this type, common logic can be implemented on the base implementation types where possible, allowing
 * for the elision of duplicitous code from prior functionality.</P>
 * <p>
 * <p>
 * <hr/>
 *
 * <H2>Component System Design</H2>
 * <P><strong>All</strong> key types in the NBComponent system must have names starting with <em>NBComponent</em>.</P>
 *
 * <P>Components are structured hierarchically. All components exist within the scope of their parent, with the only
 * exception being the root component, which has no parent. Components always know their parent from construction time.
 * After a component is constructed, it is informed of children components being added and removed via
 * {@link io.nosqlbench.nb.api.components.NBComponent#attachChild} and {@link io.nosqlbench.nb.api.components.NBComponent#detachChild}
 * methods.</P>
 *
 * <P>Component logic should interact with other components using the component interfaces and types. No contextual
 * typing or casting should be allowed within the component layer methods. Components only understand components by
 * design, and breaking this abstraction is counter-productive at best.</P>
 *
 * <P>Utility classes which understand how to interact with components should be used where possible when the
 * capabilities they provide are well-themed and cohesive. A Corollary to this is that each interface added to the core
 * component type should be solely informational about the structure and properties of the component hierarchy when
 * possible. This will help organize usage patterns around themed utilities and keep the surface area of the core types
 * to a minimum.</p>
 * <HR/>
 * <H2>Types of Components</H2>
 * The purpose of a component can determine its style of implementation.
 *
 * <H3>Life-cycle Oriented Components</H3>
 * <P>Life-cycle components represent executions of some user-managed scope, like session, scenarios, or activities.
 * These components model the nesting structure of threads of execution, or for activities, groups of threads. As such,
 * any sub-components they have are generally there to augment or contextualize the execution of the life-cycle component.
 * In this case, the life-cycle component controls the life-line of its sub-components. When the life-cycle component is
 * ready to finish its own execution, it will directly inform all components attached to it that it is time for them
 * to do final housekeeping, including any final buffering and sending, connection tear-down, etc. As components, the parent
 * component may not know what the details of these housekeeping steps are directly. But components are always something else too,
 * and in the type-specific implementations which are triggered by component methods, appropriate integrations can take place.</P>
 *
 * <H3>Service Oriented Components</H3>
 * <p>Service components are those which are created as an attachment to other components. They may or may not have additional asynchronous
 * behavior with respect to their parent component, but they are always in service of the parent component. For example, metrics instruments
 * like counters are passive, but reporters which send these metrics outside of the system are active and on a schedule.
 * Service-oriented components generally do not control the duration of their lifetimes. When they are working properly, they exist
 * along-side and attached to their parent component for the full lifetime of that parent, and then are reaped on demand by the parent
 * component.</p>
 *
 * <HR/>
 * <H2>Labeling Consistency and interlocks</H2>
 * TODO: labeling consistency
 */
package io.nosqlbench.nb.api.components;
