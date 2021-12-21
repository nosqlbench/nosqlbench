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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.HashRange;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.nio.CharBuffer;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Pseudo-randomly extract a section of a text file and return it according to some
 * minimum and maximum extract size. The file is loaded into memory as a shared
 * text image. It is then indexed into as a character buffer to find a pseudo-randomly
 * sized fragment.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class HashedFileExtractToString implements LongFunction<String> {

    private final static Logger logger = LogManager.getLogger(HashedFileExtractToString.class);


    private final CharBuffer buf;
    private final LongToIntFunction sizeFunc;
    private final LongToIntFunction positionRange = new HashRange(0, Integer.MAX_VALUE);
    private final static transient ThreadLocal<StringBuilder> tl_sb = ThreadLocal.withInitial(StringBuilder::new);
    private final String filename;

    @Example({"HashedFileExtractToString('data/adventures.txt',100,200)", "return a fragment from adventures.txt between 100 and 200 characters long"})
    public HashedFileExtractToString(String filename, int minsize, int maxsize) {
        this.filename = filename;
        this.buf = NBIO.readCharBuffer(filename).asReadOnlyBuffer();
        this.sizeFunc = new HashRange(minsize, maxsize);
    }

    /**
     * Provide a size function for the fragment to be extracted. In this form, if the size function specifies a string
     * size which is larger than the text image, it is truncated via modulo to fall within the text image size.
     *
     * @param filename The file name to be loaded
     * @param sizefunc A function which determines the size of the data to be loaded.
     */
    @Example({"HashedFileExtractToString('data/adventures.txt',Uniform())", "return a fragment from adventures.txt from a random offset, based on the size function provided."})
    public HashedFileExtractToString(String filename, Object sizefunc) {
        this.filename = filename;
        this.buf = NBIO.readCharBuffer(filename).asReadOnlyBuffer();
        sizeFunc = VirtDataConversions.adaptFunction(sizefunc, LongToIntFunction.class);
    }

    @Override
    public String apply(long input) {
        int size = sizeFunc.applyAsInt(input) % buf.limit();
        int pos = positionRange.applyAsInt(input);
        pos = pos % (buf.limit() - size); // modulo by overrun if >0
        return buf.subSequence(pos, pos + size).toString();
    }

    public String toString() {
        return getClass().getSimpleName() + ":" + filename;
    }

}
