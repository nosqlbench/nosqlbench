/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.engine.pools;

import io.nosqlbench.nb.api.lifecycle.ObjectPool;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectPoolTest {

    @Test
    public void testPoolGet() {
        ObjectPool<StringBuilder> pool = new ObjectPool<>(StringBuilder::new, sb -> sb.setLength(1));
        try (var handle = pool.get()) {
            handle.ref.append("abc");
            assertThat(pool.getActive()).isEqualTo(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (var handle2=pool.get()) {
            assertThat(pool.getActive()).isEqualTo(1);
            assertThat(handle2.ref.toString()).isEqualTo("a");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
