/*
 *
 *       Copyright 2015 Jonathan Shook
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_inetaddress;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.function.LongFunction;

/**
 * Convert the input value to a {@code java.net.InetAddress}
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ToInetAddress implements LongFunction<InetAddress> {

    @Override
    public InetAddress apply(long input) {

        int image = (int) input % Integer.MAX_VALUE;

        ByteBuffer bytes = ByteBuffer.allocate(4);
        bytes.putInt(image);
        bytes.flip();
        try {
            return Inet4Address.getByAddress(bytes.array());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
