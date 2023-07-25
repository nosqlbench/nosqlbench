/*
 * Copyright (c) 2022-2023 nosqlbench
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

import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.Hash;
import io.nosqlbench.virtdata.library.basics.shared.util.Combiner;

import java.nio.CharBuffer;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Builds a shared text image in memory and samples from it
 * pseudo-randomly with hashing.
 *
 * The characters provided can be listed like a string (abc123),
 * or can include range specifiers like a hyphen (a-zA-Z0-9).
 *
 * These characters are used to build an image of the specified size in
 * memory that is sampled from according to the size function.
 *
 * The extracted value is sized according to either a provided function, a
 * size range, or otherwise the whole image.
 *
 * The image can be varied between tests if you want by specifying a seed
 * value. If no seed value is specified, then the image length is used
 * also as a seed.
 */
@ThreadSafeMapper
@Categories(Category.general)
public class CharBufImage implements LongFunction<CharBuffer> {

    private final Hash posFunc = new Hash();

    private final CharBuffer image;
    private final int imgsize;
    private final LongToIntFunction sizefunc;

    /**
     * Shortcut constructor for building a simple text image
     * from A-Z, a-z, 0-9 and a space, of the specified size.
     * When this function is used, it always returns the full image
     * if constructed in this way.
     * @param size length in characters of the image.
     */
    public CharBufImage(int size) {
        this("a-zA-Z0-9 ",size,size);
    }

    /**
     * This is the same as {@link CharBufImage(Object,int,Object)} except that the
     * extracted sample length is fixed to the buffer size, thus the function will
     * always return the full buffer.
     * @param charsFunc The function which produces objects, which toString() is used to collect their input
     * @param imgsize The size of the CharBuffer to build at startup
     */
    public CharBufImage(Object charsFunc, int imgsize) {
        this(charsFunc, imgsize, imgsize);
    }

    /**
     * This is the same as {@link CharBufImage(Object, int, Object, long)} excep that
     * the seed is defaulted to 0L
     * @param charsFunc The function which produces objects, which toString() is used to collect their input
     * @param imgsize The size of the CharBuffer to build at startup
     * @param sizespec The specifier for how long samples should be. If this is a number, then it is static. If
     *                 it is a function, then the size is determined for each call.
     */
    public CharBufImage(Object charsFunc, int imgsize, Object sizespec) {
        this(charsFunc, imgsize, sizespec, 0L);
    }

    /**
     * Create a CharBuffer full of the contents of the results of calling a source
     * function until it is full. Then allow it to be sampled with random extracts
     * as determined by the sizespec.
     * @param charsFunc The function which produces objects, which toString() is used to collect their input
     * @param imgsize The size of the CharBuffer to build at startup
     * @param sizespec The specifier for how long samples should be. If this is a number, then it is static. If
     *                 it is a function, then the size is determined for each call.
     * @param seed      A seed that can be used to change up the rendered content.
     */
    public CharBufImage(Object charsFunc, int imgsize, Object sizespec, long seed) {
        this.imgsize = imgsize;

        LongFunction<Object> imgfunc = null;

        if (charsFunc instanceof Number) {
            throw new BasicError("The " + this.getClass().getSimpleName() + " function has an explicit size parameter. The first" +
                " parameter must be a function or string");
        } else if (charsFunc instanceof CharSequence) {
            String chars = ((CharSequence)charsFunc).toString();
            imgfunc = l -> genBuf(chars,1024, l);
        } else {
            imgfunc = VirtDataConversions.adaptFunction(charsFunc,LongFunction.class,Object.class);
        }

        this.image = this.fill(imgfunc, imgsize, seed);

        if (sizespec instanceof Number) {
            int size = ((Number)sizespec).intValue();
            this.sizefunc = n -> size;
        } else {
            this.sizefunc = VirtDataConversions.adaptFunction(sizespec,LongToIntFunction.class);
        }


    }

    private CharBuffer fill(LongFunction<Object> func, int size, long seed) {
        CharBuffer cb = CharBuffer.allocate(size);
        CharBuffer src = CharBuffer.wrap(func.apply(seed++).toString());

        while (cb.hasRemaining()) {
            if (!src.hasRemaining()) {
                src = CharBuffer.wrap(func.apply(seed++).toString());
            }
            int tosend = Math.min(src.remaining(), cb.remaining());
            src.limit(tosend);
            cb.put(src);
        }
        return cb.flip().asReadOnlyBuffer();

    }

    private CharBuffer genBuf(String chars, int size, long seed) {
        char[] charset = Combiner.rangeFor(chars);

        CharBuffer newimage = CharBuffer.allocate(size);

        io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash genhash =
            new io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash();

        while (newimage.hasRemaining()) {
            seed = genhash.applyAsLong(seed);
            int selector = (int) (seed % charset.length);
            newimage.put(charset[selector]);
        }
        return newimage.flip().asReadOnlyBuffer();
    }


    @Override
    public CharBuffer apply(long value) {
        int size = Math.min(sizefunc.applyAsInt(value), imgsize);
        int pos = posFunc.applyAsInt(value);
        pos = pos % ((imgsize - size) + 1);
        return image.subSequence(pos, pos + size);
    }
}
