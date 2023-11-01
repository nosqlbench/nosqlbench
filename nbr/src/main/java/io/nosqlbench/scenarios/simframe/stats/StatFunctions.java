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

package io.nosqlbench.scenarios.simframe.stats;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.function.ToDoubleFunction;

public class StatFunctions {
//        public static double[] lastStddev(int[] ranges, LinkedList<TimedSample> values) {
//            double[] avgs = new double[ranges.length];
//            for (int i = 0; i < ranges.length; i++) {
//                int range = ranges[i];
//                if (values.size()>=range) {
//                    double avg=0.0d;
//                    ListIterator<TimedSample> iter = listIterator(size() - range);
//                    while (iter.hasNext()) avg += (iter.next().value / range);
//                    avgs[i]=avg;
//                } else {
//                    avgs[i]=Double.NaN;
//                }
//            }
//
//        }
//
//        public static double[] stackedSample(LinkedList<TimedSample> values, ToDoubleFunction<double[]> func, int... windows) {
//            double[] results = new double[windows.length];
//            for (int i = 0; i < windows.length; i++) {
//                int range = windows[i];
//                if (values.size()>=range) {
//                    double avg=0.0d;
//                    ListIterator<TimedSample> iter = listIterator(size() - range);
//                    while (iter.hasNext()) avg += (iter.next().value / range);
//                    results[i]=avg;
//                } else {
//                    results[i]=Double.NaN;
//                }
//            }
//
//
//        }
}
