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

package io.nosqlbench.nb.api.components.status;

import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.NBComponentTraversal;
import io.nosqlbench.nb.api.components.core.NBInvokableState;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NBStatusComponent extends NBBaseComponent {
    public NBStatusComponent(NBComponent parentComponent) {
        super(parentComponent);
    }

    public NBStatusComponent(NBComponent parentComponent, NBLabels componentSpecificLabelsOnly) {
        super(parentComponent, componentSpecificLabelsOnly);
    }

    public NBStatusComponent(NBComponent parentComponent, NBLabels componentSpecificLabelsOnly, Map<String, String> props) {
        super(parentComponent, componentSpecificLabelsOnly, props);
    }

    public Status status() {

        List<Status> subbeats = new ArrayList<>();
        StatusVisitor statusVisitor = new StatusVisitor(subbeats);
        for (NBComponent child : getChildren()) {
            NBComponentTraversal.visitDepthFirstLimited(child,statusVisitor,c -> c instanceof NBStatusComponent);
        }

        return new Status(
            getLabels(),
            this.getComponentState(),
            started_epoch_ms(),
            session_time_ms(),
            0L,
            0L,
            subbeats
        );
    }

    public long session_time_ms() {
        NBInvokableState state = getComponentState();
        long nanos = switch (state) {
            case ERRORED -> (nanosof_error() - nanosof_start());
            case STARTING, RUNNING -> (System.nanoTime() - nanosof_start());
            case CLOSING -> (nanosof_close() - nanosof_start());
            case STOPPED -> (nanosof_teardown() - nanosof_start());
        };
        return nanos / 1_000_000L;
    }

    private final static class StatusVisitor implements NBComponentTraversal.FilterVisitor {

        private final List<Status> statusList;

        public StatusVisitor(List<Status> statusList) {
            this.statusList = statusList;
        }

        @Override
        public void visitMatching(NBComponent component, int depth) {
            if (component instanceof NBStatusComponent sc) {
                statusList.add(sc.status());
            }
        }

        @Override
        public void visitNonMatching(NBComponent component, int depth) {
        }
    }

}
