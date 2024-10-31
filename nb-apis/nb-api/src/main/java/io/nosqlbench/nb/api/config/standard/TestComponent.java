/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.api.config.standard;

import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestComponent extends NBBaseComponent {
    private final static Logger logger = LogManager.getLogger("RUNTIME");

    public static final NBComponent INSTANCE = new TestComponent();

    public TestComponent(String... labels) {
        super(null, NBLabels.forKV((Object[]) labels));
//        logger.trace("new component " + description());
    }

    public TestComponent(NBComponent parent, String... labels) {
        super(parent, NBLabels.forKV((Object[]) labels));
//        logger.trace("new component " + description());
    }

    @Override
    public String toString() {
        return "TestComponent #"+hashCode();
    }

    @Override
    public NBComponent attachChild(NBComponent... children) {
        for (NBComponent child : children) {
//            logger.debug("attaching " + child.description());
            super.attachChild(child);
        }
        return this;
    }

    @Override
    public NBComponent detachChild(NBComponent... children) {
        for (NBComponent child : children) {
//            logger.debug("detaching " + child.description());
            super.detachChild(child);
        }
        return this;
    }

    @Override
    public void beforeDetach() {
//        logger.debug("before detach " + description());
    }

}
