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

import java.util.Arrays;

/**
 * Provide named doubles without resorting to Map and boxing shenanigans.
 */
public class BasisValues {
    private double[] values = new double[0];
    private String[] names = new String[0];

    public double put(String name, double value) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) {
                values[i] = value;
                return value;
            }
        }
        double[] newValues = new double[values.length + 1];
        System.arraycopy(values,0,newValues,0,values.length);
        newValues[newValues.length-1]=value;
        this.values = newValues;

        String[] newNames = new String[names.length + 1];
        System.arraycopy(names,0,newNames,0,names.length);
        newNames[newNames.length-1]=name;
        this.names = newNames;

        return value;
    }

    public double get(String name) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) {
                return values[i];
            }
        }
        throw new RuntimeException("Unknown name '" + name + "': in " + Arrays.toString(names));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("values: ");
        if (values.length>0) {
            for (int i = 0; i < values.length; i++) {
                sb.append(names[i]).append("=").append(String.format("%.3f",values[i])).append(" ");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
}
