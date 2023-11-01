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

package io.nosqlbench.scenarios.simframe.capture;

import io.nosqlbench.scenarios.simframe.capture.FrameSample;

import java.util.ArrayList;
import java.util.List;

/**
 * A bundle of frame samples
 */
public class FrameSampleSet extends ArrayList<FrameSample> {
    public FrameSampleSet(List<FrameSample> samples) {
        super(samples);
    }

    public int index() {
        return getLast().index();
    }

    public double value() {
        double product = 1.0;
        for (FrameSample sample : this) {
            double weighted = sample.weightedValue();
            product *= weighted;
        }
        return product;
    }

    // https://www.w3.org/TR/xml-entity-names/025.html
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("FRAME % 5d  VALUE %10.5f\n", index(), value()));
        for (int i = 0; i < this.size(); i++) {
            sb.append(i==this.size()-1 ? "┗━▶ ": "┣━▶ ");
            sb.append(get(i).toString()).append("\n");
        }
        return sb.toString();
    }

}
