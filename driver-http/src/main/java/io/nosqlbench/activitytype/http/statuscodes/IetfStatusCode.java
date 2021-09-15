package io.nosqlbench.activitytype.http.statuscodes;

public class IetfStatusCode {
    private final String values;
    private final String description;
    private final String reference;
    private final HttpStatusRanges category;

    public IetfStatusCode(String values, String description, String reference, HttpStatusRanges category) {
        this.values = values;
        this.description = description;
        this.reference = reference;
        this.category = category;
    }

    public String getValues() {
        return values;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public HttpStatusRanges getCategory() {
        return category;
    }

    public String toString(int code) {
        if (values.equals(String.valueOf(code))) {
            return toString();
        } else {
            return code + ": " + this;
        }
    }

    public String toString() {
        String ref = reference
            .replaceFirst("\\[RFC(\\d+), Section (.+?)]","[https://www.iana.org/go/rfc$1#section-$2]") // https://www.rfc-editor.org/rfc/rfc7231.html#section-6.3.1
            .replaceFirst("\\[RFC(\\d+)(.*)]","[https://www.iana.org/go/rfc$1$2]");  // https://www.iana.org/go/rfc7231

        return (values!=null ? values : "") + (description!=null ? ", "+description :"") + ", " + ref + ", " + category.toString();
    }
}
