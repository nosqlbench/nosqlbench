package io.nosqlbench.activitytype.http.statuscodes;

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
