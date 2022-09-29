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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_uuid.ToUUID;
import io.nosqlbench.virtdata.library.basics.shared.from_uuid.ToBase64String;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ToBase64StringTest {

    @Test
    public void testApply() {
        io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.ToBase64String f = new io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.ToBase64String();
        String apply = f.apply(32144123454345L);
        assertThat(apply).isEqualTo("AAAdPCMPY4k=");
    }

    @Test
    public void testStringStringForm() {
        io.nosqlbench.virtdata.library.basics.shared.unary_string.ToBase64String f =
                new io.nosqlbench.virtdata.library.basics.shared.unary_string.ToBase64String();
        String r = f.apply("four score and seven years ago");
        assertThat(r).isEqualTo("Zm91ciBzY29yZSBhbmQgc2V2ZW4geWVhcnMgYWdv");
    }

    @Test
    public void testUuidToBase64() {
        ToUUID toUUID = new ToUUID();
        UUID uuid1 = toUUID.apply(1);
        ToBase64String toBase64 = new ToBase64String();
        String string = toBase64.apply(uuid1);
        assertThat(string).isEqualTo("ASNFZ4mrTe+AAAAAAAAAAQ==");
    }

}
