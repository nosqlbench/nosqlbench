package io.nosqlbench.activitytype.http.statuscodes;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpStatusCodesTest {

    @Test
    public void testLookup() {
        IetfStatusCode result = HttpStatusCodes.lookup(404);
        assertThat(result.getCategory()).isSameAs(HttpStatusRanges.Client_Error);
        assertThat(result.getReference()).isEqualTo("[RFC7231, Section 6.5.4]");
        assertThat(result.getValues()).isEqualTo("404");
        assertThat(result.getDescription()).isEqualTo("Not Found");
        System.out.println(result.toString(404));
        assertThat(result.toString(404)).isEqualTo("404, Not Found, [https://www.iana.org/go/rfc7231#section-6.5.4], CLIENT_ERROR (The request contains bad syntax or cannot be fulfilled.)");
    }

    @Test
    public void testUnknownCodeLookupGap() {
        IetfStatusCode result = HttpStatusCodes.lookup(496);
        assertThat(result.getCategory()).isSameAs(HttpStatusRanges.Client_Error);
        assertThat(result.getReference()).isEqualTo("[check https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml]");
        assertThat(result.getValues()).isEqualTo("496");
        assertThat(result.getDescription()).isNullOrEmpty();
        System.out.println(result.toString(496));
        assertThat(result.toString(496)).isEqualTo("496, [check https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml], CLIENT_ERROR (The request contains bad syntax or cannot be fulfilled.)");
    }

    @Test
    public void testUnknownCodeLookupRange() {
        IetfStatusCode result = HttpStatusCodes.lookup(747);
        assertThat(result.getCategory()).isSameAs(HttpStatusRanges.Unknown);
        assertThat(result.getReference()).isEqualTo("[check https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml]");
        assertThat(result.getValues()).isEqualTo("747");
        assertThat(result.getDescription()).isNullOrEmpty();
        System.out.println(result.toString(747));
        assertThat(result.toString(747)).isEqualTo("747, [check https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml], UNKNOWN_ERROR (This error type is not known based on IANA registered HTTP status codes.)");

    }

}
