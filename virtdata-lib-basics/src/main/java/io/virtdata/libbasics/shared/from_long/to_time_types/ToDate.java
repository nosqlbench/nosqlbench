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

package io.virtdata.libbasics.shared.from_long.to_time_types;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.Date;
import java.util.function.LongFunction;

/**
 * Convert the input value to a {@code Date}
 */
@ThreadSafeMapper
@Categories({Category.datetime})
public class ToDate implements LongFunction<Date> {

    private long spacing;
    private long repeat_count;

    public ToDate(int spacing, int repeat_count){
        this.spacing = spacing;
        this.repeat_count = repeat_count;
    }
    public ToDate(int spacing){
        this(spacing, 1);
    }

    public ToDate() {
        this.spacing=1;
        this.repeat_count=1;
    }

    @Override
    public Date apply(long input) {
        input = (input*spacing)/repeat_count;
        return new Date(input);
    }

    public String toString() {
        return getClass().getSimpleName() + ":" + spacing+ ":" + repeat_count;
    }
}
