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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_bytebuffer;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@Categories(Category.conversion)
@ThreadSafeMapper
public class DigestToByteBuffer implements LongFunction<ByteBuffer> {

    private static final Logger logger = LogManager.getLogger(DigestToByteBuffer.class);
    private static ThreadLocal<ThreadLocalState> state = null;

    public DigestToByteBuffer(String digestType) {

        if (!Arrays.asList(MessageDigestAlgorithms.values()).contains(digestType)) {
            throw new RuntimeException("A digest of type " + digestType +
                    " was not found. Select a digest type from: "
                    + Arrays.stream(MessageDigestAlgorithms.values()).
                    collect(Collectors.joining(",", "[", "]")));
        }

        try {
            if (digestType.equalsIgnoreCase("md5") ||digestType.equalsIgnoreCase("md2") ) {
                logger.warn("Not recommended to use 'MD5 or MD2'. A stronger message digest algorithm is recommended.");
            }

            if (state != null) {
                state.remove();
            }

            final Supplier<MessageDigest> mds = () -> {
                try {
                    return MessageDigest.getInstance(digestType);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            };
            state = ThreadLocal.withInitial(() -> new ThreadLocalState(mds));

        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: ", e);
        }

    }

    @Override
    public ByteBuffer apply(long value) {
        if (DigestToByteBuffer.state != null) {
            final ThreadLocalState tlState = DigestToByteBuffer.state.get();
            tlState.buf.putLong(0, value);
            byte[] digest = tlState.digest.digest(tlState.buf.array());
            return ByteBuffer.wrap(digest);
        }
        throw new RuntimeException("Unable to apply long value as state is not initialized.");
    }

    private static final class ThreadLocalState {
        private final MessageDigest digest;
        private final ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);

        public ThreadLocalState(Supplier<MessageDigest> mds) {
            digest = mds.get();
        }
    }
}
