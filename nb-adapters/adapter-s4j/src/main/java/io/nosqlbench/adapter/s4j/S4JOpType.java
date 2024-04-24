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

package io.nosqlbench.adapter.s4j;

public enum S4JOpType {
    // publishing/sending messages to a JMS queue or a topic
    MessageProduce,
    // consuming/receiving messages from a JMS queue or a topic
    //   for a topic, it can be:
    //     - non-durable, non-shared
    //     - durable, non-shared
    //     - non-durable, shared
    //     - durable, shared
    MessageConsume;
}


