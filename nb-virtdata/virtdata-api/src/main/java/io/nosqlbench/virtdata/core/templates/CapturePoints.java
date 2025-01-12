package io.nosqlbench.virtdata.core.templates;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CapturePoints<TYPE> extends ArrayList<CapturePoint<TYPE>> {

    public CapturePoints() {}

    public CapturePoints(@NotNull Collection<? extends CapturePoint<TYPE>> capturePoints) {
        super(capturePoints);
    }

    public boolean isGlob() {
        return this.size()==1 && this.get(0).getSourceName().equals("*");
    }

    public List<String> getSourceNames() {
        return this.stream().map(cp -> cp.getAsName()).toList();
    }
    public List<String> getAsNames() {
        return this.stream().map(cp -> cp.getAsName()).toList();
    }
}
