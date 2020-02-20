/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * &Gamma; (Gamma) and &Beta; (Beta) family of functions.
 *
 * Implementation of {@link org.apache.commons.numbers.gamma.InvGamma1pm1 InvGamma1pm1}
 * and {@link org.apache.commons.numbers.gamma.LogGamma1p LogGamma1p} is based on the
 * algorithms described in
 * <ul>
 * <li>
 *  <a href="http://dx.doi.org/10.1145/22721.23109">Didonato and Morris (1986)</a>,
 *  <em>Computation of the Incomplete Gamma Function Ratios and their Inverse</em>,
 *  TOMS 12(4), 377-393,
 * </li>
 * <li>
 *  <a href="http://dx.doi.org/10.1145/131766.131776">Didonato and Morris (1992)</a>,
 *  <em>Algorithm 708: Significant Digit Computation of the Incomplete Beta Function
 *  Ratios</em>, TOMS 18(3), 360-373,
 * </li>
 * </ul>
 * and implemented in the
 * <a href="http://www.dtic.mil/docs/citations/ADA476840">NSWC Library of Mathematical Functions</a>.
 */
package org.apache.commons.numbers.gamma;
