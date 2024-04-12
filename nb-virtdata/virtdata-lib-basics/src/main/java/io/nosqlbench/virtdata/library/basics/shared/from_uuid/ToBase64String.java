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

package io.nosqlbench.virtdata.library.basics.shared.from_uuid;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Function;

@ThreadSafeMapper
@Categories(Category.conversion)
public class ToBase64String implements Function<UUID,String> {
    private final ThreadLocal<Base64.Encoder> tl_encoder = ThreadLocal.withInitial(Base64::getEncoder);

    @Example({"ToBase64String()","Encode the bits of a UUID into a Base64 String"})
    public ToBase64String() {
    }

    @Override
    public String apply(UUID uuid) {
        ByteBuffer bytes = ByteBuffer.allocate(Long.BYTES << 1);
        bytes.putLong(uuid.getMostSignificantBits());
        bytes.putLong(uuid.getLeastSignificantBits());
        Base64.Encoder encoder = tl_encoder.get();
        byte[] encoded = encoder.encode(bytes.array());
        return new String(encoded, StandardCharsets.UTF_8);
    }
}
