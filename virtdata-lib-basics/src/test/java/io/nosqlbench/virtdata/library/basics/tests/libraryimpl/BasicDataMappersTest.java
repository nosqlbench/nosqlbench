package io.nosqlbench.virtdata.library.basics.tests.libraryimpl;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicDataMappersTest {

    @Test
    public void testGetDataMapper() throws Exception {
        Optional<DataMapper<Object>> dataMapper = VirtData.getOptionalMapper("StaticStringMapper('foo')");
        assertThat(dataMapper.isPresent()).isTrue();
        assertThat(dataMapper.get().get(5)).isEqualTo("foo");
    }

    @Test
    public void testMultipleChoiceLong() {
        Optional<DataMapper<Object>> add5 = VirtData.getOptionalMapper("long -> Add(5) -> long");
        assertThat(add5).isPresent();
        Object o = add5.get().get(5);
        assertThat(o.getClass()).isEqualTo(Long.class);
    }

    @Test
    public void testToDateSpaceInstantiator() throws Exception {
        Optional<DataMapper<Date>> dataMapper = VirtData.getOptionalMapper("ToDate(1000)");
        assertThat(dataMapper).isNotNull();
        assertThat(dataMapper.get()).isNotNull();
        Date d1 = dataMapper.get().get(1);
        Date d2 = dataMapper.get().get(2);
        assertThat(d2).isAfter(d1);
    }
    @Test
    public void testToDateSpaceAndCountInstantiator() throws Exception {
        Optional<DataMapper<Date>> dataMapper = VirtData.getOptionalMapper("ToDate(1000,10)");
        assertThat(dataMapper).isNotNull();
        assertThat(dataMapper.get()).isNotNull();
        Date d1 = dataMapper.get().get(1);
        Date d2 = dataMapper.get().get(2);
        assertThat(d2).isAfter(d1);
    }
    @Test
    public void testToDateInstantiator() throws Exception {
        Optional<DataMapper<Date>> dataMapper = VirtData.getOptionalMapper("ToDate()");
        assertThat(dataMapper).isNotNull();
        assertThat(dataMapper.get()).isNotNull();
        Date d1 = dataMapper.get().get(1);
        Date d2 = dataMapper.get().get(2);
        assertThat(d2).isAfter(d1);
    }

    @Test
    public void testToDateBucketInstantiator() throws Exception {
        Optional<DataMapper<Date>> dataMapper = VirtData.getOptionalMapper("ToDate(1000,10000)");
        assertThat(dataMapper).isNotNull();
        assertThat(dataMapper.get()).isNotNull();
        assertThat(dataMapper.get().get(0)).isEqualTo(new Date(0));
        assertThat(dataMapper.get().get(10)).isEqualTo(new Date(1));
        assertThat(dataMapper.get().get(20)).isEqualTo(new Date(2));
    }

    @Test
    public void testRandomLineToIntInstantiator() throws Exception {
        Optional<DataMapper<Integer>> dataMapper = VirtData.getOptionalMapper("HashedLineToInt('data/numbers.txt')");
        assertThat(dataMapper).isNotNull();
        assertThat(dataMapper.get()).isNotNull();
        assertThat(dataMapper.get().get(1)).isNotNull();
    }
}
