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

package io.nosqlbench.activitytype.http.statuscodes;

import io.nosqlbench.adapter.http.statuscodes.HttpStatusCodes;
import io.nosqlbench.adapter.http.statuscodes.HttpStatusRanges;
import io.nosqlbench.adapter.http.statuscodes.IetfStatusCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpStatusCodesTest {
    private final static Logger logger = LogManager.getLogger(HttpStatusCodesTest.class);

    @Test
    public void testLookup() {
        IetfStatusCode result = HttpStatusCodes.lookup(404);
        assertThat(result.getCategory()).isSameAs(HttpStatusRanges.Client_Error);
        assertThat(result.getReference()).isEqualTo("[RFC7231, Section 6.5.4]");
        assertThat(result.getValues()).isEqualTo("404");
        assertThat(result.getDescription()).isEqualTo("Not Found");
        logger.debug(() -> result.toString(404));
        assertThat(result.toString(404)).isEqualTo("404, Not Found, [https://www.iana.org/go/rfc7231#section-6.5.4], CLIENT_ERROR (The request contains bad syntax or cannot be fulfilled.)");
    }

    @Test
    public void testUnknownCodeLookupGap() {
        IetfStatusCode result = HttpStatusCodes.lookup(496);
        assertThat(result.getCategory()).isSameAs(HttpStatusRanges.Client_Error);
        assertThat(result.getReference()).isEqualTo("[check https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml]");
        assertThat(result.getValues()).isEqualTo("496");
        assertThat(result.getDescription()).isNullOrEmpty();
        logger.debug(() -> result.toString(496));
        assertThat(result.toString(496)).isEqualTo("496, [check https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml], CLIENT_ERROR (The request contains bad syntax or cannot be fulfilled.)");
    }

    @Test
    public void testUnknownCodeLookupRange() {
        IetfStatusCode result = HttpStatusCodes.lookup(747);
        assertThat(result.getCategory()).isSameAs(HttpStatusRanges.Unknown);
        assertThat(result.getReference()).isEqualTo("[check https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml]");
        assertThat(result.getValues()).isEqualTo("747");
        assertThat(result.getDescription()).isNullOrEmpty();
        logger.debug(() -> result.toString(747));
        assertThat(result.toString(747)).isEqualTo("747, [check https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml], UNKNOWN_ERROR (This error type is not known based on IANA registered HTTP status codes.)");

    }

}
