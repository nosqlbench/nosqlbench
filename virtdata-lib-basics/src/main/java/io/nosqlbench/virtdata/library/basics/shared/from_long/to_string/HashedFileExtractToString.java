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
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.HashRange;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.nio.CharBuffer;
import java.util.function.LongFunction;

/**
 * Pseudo-randomly extract a section of a text file and return it according to some
 * minimum and maximum extract size. The file is loaded into memory as a shared
 * text image. It is then indexed into as a character buffer to find a pseudo-randomly
 * sized fragment.
 */
@ThreadSafeMapper
public class HashedFileExtractToString implements LongFunction<String> {

    private final static Logger logger  = LogManager.getLogger(HashedFileExtractToString.class);

    private static CharBuffer fileDataImage =null;
    private final HashRange sizeRange;
    private final HashRange positionRange;

    private int minsize, maxsize;
    private final String fileName;

    @Example({"HashedFileExtractToString('data/adventures.txt',100,200)","return a fragment from adventures.txt between 100 and 200 characters long"})
    public HashedFileExtractToString(String fileName, int minsize, int maxsize) {
        this.fileName = fileName;
        this.minsize = minsize;
        this.maxsize = maxsize;
        loadData();
        this.sizeRange = new HashRange(minsize, maxsize);
        this.positionRange = new HashRange(1, (fileDataImage.limit()-maxsize)-1);
    }

    private void loadData() {
        if (fileDataImage == null) {
            synchronized (HashedFileExtractToString.class) {
                if (fileDataImage == null) {
                    CharBuffer image= NBIO.readCharBuffer(fileName);
                    fileDataImage = image;
                }
            }
        }
    }

    @Override
    public String apply(long input) {


        int offset = positionRange.applyAsInt(input);
        int length = sizeRange.applyAsInt(input);
        String sub = null;
        try {
            sub = fileDataImage.subSequence(offset, offset + length).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sub;

    }

    public String toString() {
        return getClass().getSimpleName() + ":" + minsize + ":" + maxsize;
    }

}
